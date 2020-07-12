package gallifrey.backend;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gallifrey.core.Frontend;
import gallifrey.core.RMIInterface;
import gallifrey.core.VectorClock;
import gallifrey.core.CentralDudeInterface;

import eu.antidotedb.client.GenericKey;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

// Todo More descriptive name to be thought of
public class CentralDude extends UnicastRemoteObject implements CentralDudeInterface {
    private static final long serialVersionUID = 777L;
    private final List<Frontend> antidote_frontends;
    private final List<RMIInterface> antidote_backends;

    public CentralDude(List<String> antidotes, List<Integer> ports, String bucket, List<String> antidote_backends)
            throws RemoteException {
        List<Frontend> frontends = new ArrayList<>();
        assert (antidotes.size() == ports.size());
        for (int i = 0; i < antidotes.size(); i++) {
            frontends.add(new Frontend(antidotes.get(i), ports.get(i), bucket));
        }
        this.antidote_frontends = frontends;

        List<RMIInterface> backends = new ArrayList<>();
        for (String backend : antidote_backends) {
            try {
                backends.add((RMIInterface) Naming.lookup(backend));
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                System.out.println("Something happend when trying to look up the backend");
                e.printStackTrace();
                System.exit(123456);
            }
        }
        this.antidote_backends = backends;
    }

    @Override
    public void transition(GenericKey key, String restriction) {
        // Get writelocks from backends
        antidote_backends.parallelStream().forEach(backend -> {
            try {
                backend.writeLockRestriction(key);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        // Issue reads through the frontend to flush operations
        antidote_frontends.parallelStream().forEach(frontend -> frontend.static_read(key));
        // Read the current update time of the object
        List<VectorClock> vec_list = antidote_backends.parallelStream().map(backend -> {
            try {
                return backend.getCurrentTime(key);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        // Create max vectorclock time for the block time
        VectorClock maxClock = VectorClock.max(vec_list);
        // Set block until on all backends
        antidote_backends.parallelStream().forEach(backend -> {
            try {
                backend.setBlockUntilTime(key, maxClock, restriction);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        String config;
        if (args.length >= 1) {
            config = args[0];
        } else {
            config = "central_dude_config.json";
        }

        JSONParser parser = new JSONParser();

        ArrayList<String> hostnames = new ArrayList<String>();
        ArrayList<Integer> ports = new ArrayList<Integer>();
        ArrayList<String> backends = new ArrayList<String>();

        try {
            JSONObject jsonConfig = (JSONObject) parser.parse(new FileReader(config));

            JSONArray frontendiInfoPairs = (JSONArray) jsonConfig.get("Frontends");
            for (Object infoPair : frontendiInfoPairs) {
                hostnames.add((String) ((JSONObject) infoPair).get("hostname"));
                ports.add((Integer) ((JSONObject) infoPair).get("port"));
            }

            String bucket = (String) jsonConfig.get("Bucket");

            JSONArray jsonBackend = (JSONArray) jsonConfig.get("Backends");
            for (Object b : jsonBackend) {
                backends.add((String) b);
            }

            CentralDude dude = new CentralDude(hostnames, ports, bucket, backends);
            LocateRegistry.createRegistry(1099); // 1099 is the default port
            String central_dude_hostname = System.getenv("CENTRAL_DUDE_HOSTNAME");
            if (central_dude_hostname == null) {
                central_dude_hostname = "127.0.0.1";
            }
            System.setProperty("java.rmi.server.hostname", central_dude_hostname);
            String central_dude_name = System.getenv("CENTRAL_DUDE");
            if (central_dude_name == null) {
                central_dude_name = "/CentralDude";
            }
            Naming.rebind(central_dude_name, dude);
            // Now just keep the central dude available forever
            while (true) {
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.exit(101);
        }
    }
}
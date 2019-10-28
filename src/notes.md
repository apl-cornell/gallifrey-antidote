# Erlang function to recieve messages
```
tester2() ->
    {'javamailbox', 'JavaNode@dhcp-gs-1201'} ! {self(), {hello}},
    receive
        stop -> ok;
		error -> hi;
        Msg ->
            io:format("message:~w~n", [Msg]),
			Msg
    end.
```

# Erlang shell inputs
```
erl -sname ErlNode

net_kernel:connect_node('JavaNode@dhcp-gs-1201').

c(mailbox).
mailbox:tester2().
```
Here mailbox is the module containing the tester2/0 function. Maybe we can move the connect_node inside the function
DFN WATCHDOG
============

Modules
-------
* watchdog
* watchdog-agent
* watchdog-client
* watchdog-commons
* reconciliation
  
####Introduction
Watchdog is the monitoring and controlling component of the DFN NTP. 
Watchdog consists of 3 main components. They are server, agent and client. 
Watchdog server is the heart of watchdog. It listens for the connections from the agent components.
Agent is a library where other components can include so they can use the Watchdog services. 
Client component is a web server which serves the monitoring and management interface to the users.


***
###watchdog
Server module.
Check system components health.

###watchdog-agent
Agent module.
Connects to the server.
Callbacks on status changes.

###watchdog-client
Client module.
Monitoring tool for the watchdog server.

###watchdog-commons
Common components to other modules.

###reconciliation
Reconciliation module.
Checks FIX logs and OMS audit logs for differences.


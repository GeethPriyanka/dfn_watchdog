CREATE TABLE watchdog.clientroutes (
	client_id bigint,
	next_node int,
	PRIMARY KEY (client_id)
);

INSERT INTO watchdog.clientroutes(client_id, next_node) values(10001, 1);

INSERT INTO watchdog.clientroutes(client_id, next_node) values(10002, 2);

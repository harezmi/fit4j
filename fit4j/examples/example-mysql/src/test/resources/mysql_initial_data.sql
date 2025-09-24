DROP TABLE IF EXISTS foo;

CREATE TABLE foo (
     id INT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(255) NOT NULL
);

INSERT INTO foo (name) VALUES ('foo-1');
INSERT INTO foo (name) VALUES ('foo-2');
INSERT INTO foo (name) VALUES ('foo-3');
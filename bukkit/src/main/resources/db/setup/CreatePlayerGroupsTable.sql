CREATE TABLE IF NOT EXISTS PlayerGroups (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
  "uuid" TEXT,
  "name" TEXT,
  "world" TEXT,
  "groups" TEXT
);
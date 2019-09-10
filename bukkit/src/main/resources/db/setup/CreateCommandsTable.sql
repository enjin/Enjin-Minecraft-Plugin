CREATE TABLE IF NOT EXISTS Commands (
    "id" INTEGER PRIMARY KEY UNIQUE,
    "command" TEXT,
    "delay" INTEGER,
    "requireOnline" INTEGER,
    "playerName" TEXT,
    "playerUuid" TEXT,
    "hash" TEXT,
    "response" TEXT,
    "createdAt" INTEGER
);
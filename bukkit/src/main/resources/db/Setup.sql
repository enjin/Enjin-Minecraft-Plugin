CREATE TABLE IF NOT EXISTS ExecutedCommands (
    "id" INTEGER PRIMARY KEY UNIQUE,
    "hash" TEXT,
    "response" TEXT,
    "command" TEXT
);
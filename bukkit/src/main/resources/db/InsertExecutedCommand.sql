INSERT OR IGNORE INTO Commands (id,
                                hash,
                                response,
                                command,
                                delay,
                                requireOnline,
                                playerName,
                                playerUuid,
                                createdAt)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
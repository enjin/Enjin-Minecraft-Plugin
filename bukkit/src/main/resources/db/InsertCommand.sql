INSERT OR IGNORE INTO Commands (id,
                                command,
                                delay,
                                requireOnline,
                                playerName,
                                playerUuid,
                                hash,
                                response,
                                createdAt)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
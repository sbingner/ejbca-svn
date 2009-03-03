-- Note!
-- If using UTF-8 character enoding in MySQL index length is limited to 1000 bytes, and UTF-8 characters take up 3 bytes.
create index message_idx1 on message (status);
create index message_idx2 on message (messageId);

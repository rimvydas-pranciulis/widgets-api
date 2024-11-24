CREATE UNIQUE INDEX ON widget_connections
    (LEAST(widget1_id, widget2_id), GREATEST(widget1_id, widget2_id));
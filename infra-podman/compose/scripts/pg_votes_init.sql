\c db_votes;

create table tb_votes_log (
    id uuid default gen_random_uuid() not null primary key,
    vote_id uuid,
    status varchar(20),
    error_message varchar(500),
    processed_at timestamp without time zone,
    voter_id varchar(20),
    poll_id varchar(20),
    option_id varchar(20),
    voted_at timestamp without time zone default current_timestamp
);

create table tb_votes_total (
    id uuid default gen_random_uuid() not null primary key,
    poll_id varchar(20),
    option_id varchar(20),
    total int default 0,
    created_at timestamp without time zone default current_timestamp,
    updated_at timestamp without time zone default current_timestamp
);

create index if not exists idx_votes_log_voter_poll ON public.tb_votes_log (voter_id, poll_id);
create index if not exists idx_votes_total_poll_option ON public.tb_votes_total (poll_id, option_id);

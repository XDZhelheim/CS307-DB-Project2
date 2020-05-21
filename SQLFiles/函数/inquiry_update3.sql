create function inquiry_update3() returns trigger
    language plpgsql
as
$$
begin
    delete from inquire_table
    where schedule_id = old.schedule_id;
    return new;
end;
$$;
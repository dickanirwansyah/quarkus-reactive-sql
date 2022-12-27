package com.rnd.service;

import com.rnd.entity.Account;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class AccountService {

    public Multi<Account> getAll(PgPool pgPoolClient){
        log.info("get all data");
       return pgPoolClient.query("select * from accounts order by id desc").execute()
                .onItem()
                .transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(AccountService::convertAccountRow);
    }

    public Uni<Account> getSingle(PgPool pgPoolClient, String id){
        log.info("get single by id={}",id);
        return pgPoolClient.preparedQuery("select * from accounts where id=$1")
                .execute(Tuple.of(id))
                .onItem()
                .transform(result -> result.iterator().hasNext() ? convertAccountRow(result.iterator().next()) : null);
    }

    public Uni<Account> save(PgPool pgPoolClient, Account requestAccount){
        log.info("save data={}",requestAccount);
        return pgPoolClient.preparedQuery("insert into accounts (id, username, email, mobile_number) values ($1, $2, $3, $4) returning id, username, email, mobile_number")
                .execute(Tuple.of(
                        requestAccount.getId(),
                        requestAccount.getUsername(),
                        requestAccount.getEmail(),
                        requestAccount.getMobileNumber()))
                .onItem()
                .transform(result -> result.iterator().hasNext() ? convertAccountRow(result.iterator().next()) : null);
    }

    private static Account convertAccountRow(Row row){
        return Account.builder()
                .id(row.getString("id"))
                .email(row.getString("email"))
                .username(row.getString("username"))
                .mobileNumber(row.getString("mobile_number"))
                .build();
    }

}

package com.rnd.resource;

import com.rnd.entity.Account;
import com.rnd.service.AccountService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/api/v1/accounts")
public class AccountResource {

    @Inject
    private PgPool pgPoolClient;

    @Inject
    private AccountService accountService;

    @PostConstruct
    public void config(){
        initDB();
    }

    @GET
    public Multi<Account> findAll(){
        return accountService.getAll(pgPoolClient);
    }

    @GET
    @Path("/single/{id}")
    public Uni<Response> getOne(@PathParam("id")String id){
        return accountService.getSingle(pgPoolClient, id)
                .onItem().ifNotNull()
                .transform(account -> Response.ok().entity(account)
                        .status(Response.Status.CREATED)
                        .build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)
                        .entity(new Account()).build());
    }


    @POST
    @Path("/save")
    public Uni<Account> save(Account account){
        return accountService.save(pgPoolClient, account);
    }

    private void initDB(){
        pgPoolClient.query("DROP TABLE IF EXISTS accounts").execute()
                .flatMap(c -> pgPoolClient.query("create table accounts (id varchar(100), username varchar(255), email varchar(255), mobile_number varchar(15))").execute())
                .flatMap(c -> pgPoolClient.query("insert into accounts (id, username, email, mobile_number) values ('A001', 'dickanirwansyah', 'dickanirwansyah@gmail.com', '081324366585')").execute())
                .flatMap(c -> pgPoolClient.query("insert into accounts (id, username, email, mobile_number) values ('A002', 'dickyadriansyah', 'dicky@gmail.com', '081324366586')").execute())
                .await().indefinitely();
    }
}

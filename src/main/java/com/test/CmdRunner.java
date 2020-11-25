package com.test;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.Transactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Components of the type CommandLineRunner are called right after
 * the application start up. So the method *run* is called as soon as
 * the application starts.
 */
@Component
public class CmdRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cluster cluster;

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;




    @Override
    public void run(String... strings) throws Exception {

        try {
            cluster.queryIndexes().createPrimaryIndex(couchbaseTemplate.getBucketName());
        } catch( Exception e ) {

            System.out.println("----------------> PRIMARY INDEX ALREADY EXISTS");
        }



        User u1 = createUser("user::0001", "john", "john.mason@acme.com", "password",
                Arrays.asList(new Submission("id1", "userId1", "talkId1", "status1", 1l)));
        userRepository.save(u1);
        System.out.println("********* Find users by id");
        Optional<User> user = userRepository.findById("user::0001");
        System.out.println(user.get());


        System.out.println("********* Find users by email");
        System.out.println(userRepository.findByEmailLike("john.mason%"));

    }



    public List<User> listEventTags() {

        String queryString =  "Select meta(s).id as id, s.* from "+couchbaseTemplate.getBucketName()+" s where s._class='com.test.User'" ;
        return cluster.query(queryString, QueryOptions.queryOptions().adhoc(true)
                .scanConsistency(QueryScanConsistency.REQUEST_PLUS) ).rowsAs(User.class);
    }

    @Autowired
    private Transactions transactions;
    @Autowired
    private CouchbaseClientFactory couchbaseClientFactory;

    public void transferCredit(String user1, String user2, int creditsToTransfer) {
        transactions.run(ctx -> {

            TransactionGetResult u1DocTx = ctx.get(couchbaseClientFactory.getDefaultCollection(), user1);
            TransactionGetResult u2DocTx = ctx.get(couchbaseClientFactory.getDefaultCollection(), user2);
            JsonObject u1Doc = u1DocTx.contentAs(JsonObject.class);
            JsonObject u2Doc = u2DocTx.contentAs(JsonObject.class);

            u1Doc.put("credits", u1Doc.getInt("credits") - creditsToTransfer);
            u2Doc.put("credits", u2Doc.getInt("credits") + creditsToTransfer);
            ctx.replace(u1DocTx, u1Doc);
            ctx.replace(u2DocTx, u2Doc);

            ctx.commit();
        });
    }

    public static User createUser(String id, String username, String email,
                                  String password, List<Submission> submissions) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCredits(50);
        user.setSubmissions(submissions);
        return user;
    }


}

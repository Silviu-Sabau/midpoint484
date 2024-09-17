package net.tirasa.connid.bundles.servicenow.main;

import net.tirasa.connid.bundles.servicenow.service.SNClient;
import net.tirasa.connid.bundles.servicenow.service.SNService;

public class Main {
    public static void main(String[] args) {
        SNService service = new SNService();

        // Instantiate SNClient with the service
        SNClient client = new SNClient(service);

        // Call addUserToGroup method
        String userId = "exampleUserId";
        String groupId = "exampleGroupId";
        client.addUserToGroup(userId, groupId);


    }
}

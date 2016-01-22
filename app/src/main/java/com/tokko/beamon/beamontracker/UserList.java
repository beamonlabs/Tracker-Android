package com.tokko.beamon.beamontracker;

import java.util.ArrayList;

public class UserList extends ArrayList<User> {

    @Override
    public boolean add(User user) {
        if(contains(user)) remove(user);
        return super.add(user);
    }
}

package com.example.awordfromachild.asynctask;

import twitter4j.User;

public interface callBacksNoti extends callBacksBase {
    void callBackGetUser(User user, String howToDisplay);
}

package com.example.csci571andriodstocks;

import java.util.ArrayList;
import java.util.List;

public class LoadCompany {

    public List<Company> getContactsWithLetter() {
        final List<Company> contactsList = new ArrayList<>();

        contactsList.add(new Company("Microsoft", "MSFT", 100, 201.22, 10.12));
        contactsList.add(new Company("Advanced Micro Inc.", "AMD", 10, 100.12, -5.12));

        return contactsList;
    }

}

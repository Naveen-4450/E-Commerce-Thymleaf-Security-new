package com.example.services;

import com.example.config.CustomUserDetails;
import com.example.models.dbModels.Credentials;
import com.example.repositories.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CredentialService implements UserDetailsService
{
    @Autowired
    private CredentialRepository credRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        Credentials credentials = credRepo.findByUsername(username);
        if(credentials == null){
            throw new UsernameNotFoundException("User Not Found");
        }

        return new CustomUserDetails(credentials);
    }
}

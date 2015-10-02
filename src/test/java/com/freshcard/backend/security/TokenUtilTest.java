package com.freshcard.backend.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by willy on 02.09.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "classpath:applicationContext.xml",
                "classpath:dispatcher-servlet.xml",
                "file:src/main/webapp/WEB-INF/spring-security.xml"
        }
)
public class TokenUtilTest {
    private UserDetails userDetails;

    @Before
    public void setup() {
        userDetails = new UserDetails() {
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            public String getPassword() {
                return "admin";
            }

            public String getUsername() {
                return "admin@freshcard.co";
            }

            public boolean isAccountNonExpired() {
                return false;
            }

            public boolean isAccountNonLocked() {
                return false;
            }

            public boolean isCredentialsNonExpired() {
                return false;
            }

            public boolean isEnabled() {
                return false;
            }
        };
    }

    @Test
    public void createToken() {
        assertTrue(TokenUtil.createToken(userDetails).startsWith("admin@freshcard.co:"));
    }

    @Test
    public void computeSignature() {
        assertNotNull(
                TokenUtil.computeSignature(
                        userDetails,
                        System.currentTimeMillis() + 1000l * 60 * 60 * 24 * 30
                )
        );
    }

    @Test
    public void getUserNameFromToken() {
        assertEquals("admin@freshcard.co", TokenUtil.getUserNameFromToken("admin@freshcard.co:1409775992133:79248c2b49c5a5448367318ac674829b"));
    }

    @Test
    public void validateToken() {
        assertTrue(TokenUtil.validateToken(TokenUtil.createToken(userDetails), userDetails));
    }
}

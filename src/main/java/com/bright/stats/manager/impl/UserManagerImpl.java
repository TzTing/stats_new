package com.bright.stats.manager.impl;

import com.bright.stats.manager.UserManager;
import com.bright.stats.pojo.po.primary.RoleFunction;
import com.bright.stats.pojo.po.primary.RoleMain;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.repository.primary.RoleFunctionRepository;
import com.bright.stats.repository.primary.RoleMainRepository;
import com.bright.stats.repository.second.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * @Author txf
 * @Date 2022/7/27 14:51
 * @Description
 */
@Component
@RequiredArgsConstructor
public class UserManagerImpl implements UserManager {

    private final UserRepository userRepository;
    private final RoleMainRepository roleMainRepository;
    private final RoleFunctionRepository roleFunctionRepository;

    @Override
    public User getUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        Example<User> userExample = Example.of(user);
        User userOptional = userRepository.findOne(userExample).get();

        if(Objects.nonNull(userOptional)){
            Set<RoleMain> roleMains = roleMainRepository.findRoleMain(userOptional.getTjRoleRno());
            Set<RoleFunction> roleFunctions = roleFunctionRepository.findRoleFunction(userOptional.getTjRoleRno());
            userOptional.setRoleMains(roleMains);
            userOptional.setRoleFunctions(roleFunctions);
        }
        return userOptional;
    }
}

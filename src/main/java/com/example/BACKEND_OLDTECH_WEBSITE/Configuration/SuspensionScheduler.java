package com.example.BACKEND_OLDTECH_WEBSITE.Configuration;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SuspensionScheduler {
    private static final Logger log = LoggerFactory.getLogger(SuspensionScheduler.class);

    private final UserRepository userRepository;

    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    @Transactional
    public void checkExpiredSuspensions() {
        log.info("Running scheduled check for expired suspensions");
        LocalDateTime now = LocalDateTime.now();

        // TIM TOAN BO USER HET HAN DC
        List<User> expiredSuspensions = userRepository.findByAccountStatusAndSuspensionEndTimeBefore(
                AccountStatusEnum.Suspended, now);

        if (!expiredSuspensions.isEmpty()) {
            log.info("Found {} suspended accounts eligible for automatic reactivation", expiredSuspensions.size());

            for (User user : expiredSuspensions) {
                //  KICH HOAT LAI TK
                user.setAccountStatus(AccountStatusEnum.Active);
                user.setSuspensionEndTime(null);
                user.setSuspensionReason(null);
                userRepository.save(user);

                log.info("Automatically reactivated account for user ID: {} (email: {})",
                        user.getUserId(), user.getEmail());
            }
        } else {
            log.debug("No suspended accounts found eligible for reactivation");
        }
    }
}

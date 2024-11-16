package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Notification;
import com.home.service.models.User;
import com.home.service.models.enums.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndReadStatus(User recipient, Boolean readStatus);

    List<Notification> findByRecipientAndTypeAndReadStatus(User recipient, NotificationType type, Boolean readStatus);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.type = :type")
    List<Notification> findByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);
}

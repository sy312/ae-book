package com.c201.aebook.api.notification.service.impl;

import com.c201.aebook.api.book.persistence.entity.BookEntity;
import com.c201.aebook.api.book.persistence.repository.BookRepository;
import com.c201.aebook.api.notification.persistence.entity.NotificationEntity;
import com.c201.aebook.api.notification.persistence.repository.NotificationRepository;
import com.c201.aebook.api.notification.presentation.dto.response.NotificationBookDetailResponseDTO;
import com.c201.aebook.api.notification.presentation.dto.response.NotificationBookListResponseDTO;
import com.c201.aebook.api.notification.service.NotificationService;
import com.c201.aebook.api.user.persistence.entity.UserEntity;
import com.c201.aebook.api.user.persistence.repository.UserRepository;
import com.c201.aebook.api.vo.NotificationSO;
import com.c201.aebook.converter.NotificationConverter;
import com.c201.aebook.utils.exception.CustomException;
import com.c201.aebook.utils.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    private final NotificationRepository notificationRepository;
    private final NotificationConverter notificationConverter;

    @Override
    public void saveNotification(String userId, NotificationSO notificationSO) {
        // 1. isbn 유효성 검증
        BookEntity bookEntity = bookRepository.findByIsbn(notificationSO.getIsbn())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
        // log.info("book : {}", bookEntity.getIsbn());

        // 2. userId 유효성 검증
        UserEntity userEntity = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 해당 책에 알림 신청을 한 적이 있는지 검증
        NotificationEntity notificationEntity = notificationRepository
                .findByUserIdAndBookId(userEntity.getId(), bookEntity.getId());
        if(notificationEntity != null) {
            throw new CustomException(ErrorCode.DUPLICATED_NOTIFICATION);
        }

        // 4. 알림 신청 저장
        notificationRepository.save(NotificationEntity.builder()
                .upperLimit(notificationSO.getUpperLimit())
                .user(userEntity)
                .book(bookEntity)
                .build());
    }

    @Override
    public Page<NotificationBookListResponseDTO> getMyNotificationBookList(String userId, Pageable pageable) {
        // 사용자가 알림 신청된 책 목록 가져오기
        Page<NotificationEntity> notifications = notificationRepository.findByUserId(Long.valueOf(userId), pageable);

        return notifications.map(notification -> NotificationBookListResponseDTO.builder()
                .id(notification.getId())
                .upperLimit(notification.getUpperLimit())
                .title(notification.getBook().getTitle())
                .isbn(notification.getBook().getIsbn())
                .price(notification.getBook().getPrice())
                .coverImageUrl(notification.getBook().getCoverImageUrl())
                .build());
//        return notifications.map(notification -> notificationConverter.toNotificationBookListResponseDTO(notification));
    }

    @Override
    public NotificationBookDetailResponseDTO getMyNotificationBookDetail(Long notificationId) {
        // 사용자가 알림 신청한 내역 가져오기
        NotificationEntity notificationEntity = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        log.info("notification : {}", notificationEntity.getBook().getTitle());

        return NotificationBookDetailResponseDTO.builder()
                .id(notificationId)
                .upperLimit(notificationEntity.getUpperLimit())
                .createdAt(notificationEntity.getCreatedAt())
                .updatedAt(notificationEntity.getUpdatedAt())
                .title(notificationEntity.getBook().getTitle())
                .author(notificationEntity.getBook().getAuthor())
                .publisher(notificationEntity.getBook().getPublisher())
                .isbn(notificationEntity.getBook().getIsbn())
                .price(notificationEntity.getBook().getPrice())
                .coverImageUrl(notificationEntity.getBook().getCoverImageUrl())
                .aladinUrl(notificationEntity.getBook().getAladinUrl())
                .build();
    }
}

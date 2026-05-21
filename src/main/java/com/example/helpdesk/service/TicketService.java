package com.example.helpdesk.service;

import com.example.helpdesk.domain.Category;
import com.example.helpdesk.domain.Impact;
import com.example.helpdesk.domain.Importance;
import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketAttachment;
import com.example.helpdesk.domain.TicketComment;
import com.example.helpdesk.domain.TicketHistory;
import com.example.helpdesk.domain.Urgency;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.domain.DynamicFilter;
import com.example.helpdesk.domain.DynamicFilterValue;
import com.example.helpdesk.domain.TicketDynamicValue;

import com.example.helpdesk.dto.DashboardStats;
import com.example.helpdesk.repository.PriorityWeightRepository;
import com.example.helpdesk.repository.TicketAttachmentRepository;
import com.example.helpdesk.repository.TicketCommentRepository;
import com.example.helpdesk.repository.TicketHistoryRepository;
import com.example.helpdesk.repository.TicketRepository;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.repository.DynamicFilterRepository;
import com.example.helpdesk.repository.DynamicFilterValueRepository;

import org.springframework.http.MediaTypeFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PriorityWeightRepository priorityWeightRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketAttachmentRepository ticketAttachmentRepository;
    private final DynamicFilterRepository dynamicFilterRepository;
    private final DynamicFilterValueRepository dynamicFilterValueRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
            PriorityWeightRepository priorityWeightRepository, TicketHistoryRepository ticketHistoryRepository,
            TicketCommentRepository ticketCommentRepository, TicketAttachmentRepository ticketAttachmentRepository,
            DynamicFilterRepository dynamicFilterRepository,
            DynamicFilterValueRepository dynamicFilterValueRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.priorityWeightRepository = priorityWeightRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketAttachmentRepository = ticketAttachmentRepository;
        this.dynamicFilterRepository = dynamicFilterRepository;
        this.dynamicFilterValueRepository = dynamicFilterValueRepository;
    }

    @Transactional
    public Ticket createTicket(String title, String description, String creatorUsername, Importance importance,
            Urgency urgency, Impact impact, Category category, java.util.Map<Integer, Integer> dynamicValues) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator user not found: " + creatorUsername));

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCreator(creator);
        ticket.setStatus(Ticket.Status.NEW);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setImportance(importance);
        ticket.setUrgency(urgency);
        ticket.setImpact(impact);
        ticket.setCategory(category);

        if (dynamicValues != null) {
            for (java.util.Map.Entry<Integer, Integer> entry : dynamicValues.entrySet()) {
                Integer filterId = entry.getKey();
                Integer valueId = entry.getValue();

                DynamicFilter filter = dynamicFilterRepository.findById(filterId).orElse(null);
                DynamicFilterValue value = dynamicFilterValueRepository.findById(valueId).orElse(null);

                if (filter != null && value != null) {
                    TicketDynamicValue tdv = new TicketDynamicValue();
                    tdv.setTicket(ticket);
                    tdv.setFilter(filter);
                    tdv.setValue(value);
                    ticket.getDynamicValues().add(tdv);
                }
            }
        }

        if (urgency != null) {
            switch (urgency) {
                case CRITICAL:
                    ticket.setSlaDeadline(ticket.getCreatedAt().plusHours(4));
                    break;
                case HIGH:
                    ticket.setSlaDeadline(ticket.getCreatedAt().plusHours(8));
                    break;
                case MEDIUM:
                    ticket.setSlaDeadline(ticket.getCreatedAt().plusHours(24));
                    break;
                case LOW:
                    ticket.setSlaDeadline(ticket.getCreatedAt().plusHours(72));
                    break;
            }
        }

        ticket = ticketRepository.save(ticket);

        // Расчет priorityScore
        double score = calculatePriorityScore(ticket);
        ticket.setPriorityScore(score);

        ticket = ticketRepository.save(ticket);
        recalculateAllPriorities();

        logHistory(ticket, creator, "CREATED", "Заявка создана");
        return ticket;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAllTickets(Sort sort) {
        return ticketRepository.findAll(sort);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAllTickets(boolean includeClosed, Sort sort) {
        if (includeClosed) {
            return ticketRepository.findAll(sort);
        }
        return ticketRepository.findByStatusNot(Ticket.Status.CLOSED, sort);
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> findTicketById(Integer id) {
        return ticketRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByCreator(String creatorUsername, Sort sort) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator user not found: " + creatorUsername));
        return ticketRepository.findByCreator(creator, sort);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByCreator(String creatorUsername, boolean includeClosed, Sort sort) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator user not found: " + creatorUsername));
        if (includeClosed) {
            return ticketRepository.findByCreator(creator, sort);
        }
        return ticketRepository.findByCreatorAndStatusNot(creator, Ticket.Status.CLOSED, sort);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByExecutorUsername(String executorUsername, boolean includeClosed, Sort sort) {
        if (executorUsername == null || executorUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("executorUsername is required");
        }
        String username = executorUsername.trim();
        if (includeClosed) {
            return ticketRepository.findByExecutor_Username(username, sort);
        }
        return ticketRepository.findByExecutor_UsernameAndStatusNot(username, Ticket.Status.CLOSED, sort);
    }

    @Transactional
    public Ticket updateTicketStatus(Integer ticketId, Ticket.Status newStatus, String updaterUsername,
            String resolution) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        if (newStatus == Ticket.Status.CLOSED) {
            if (resolution == null || resolution.trim().isEmpty()) {
                throw new IllegalArgumentException("Решение обязательно при закрытии заявки.");
            }
            ticket.setResolution(resolution);
            ticket.setClosedAt(LocalDateTime.now());
        } else {
            // Если заявка была закрыта, а теперь переоткрывается
            if (ticket.getStatus() == Ticket.Status.CLOSED) {
                ticket.setClosedAt(null);
            }
        }

        Ticket.Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket = ticketRepository.save(ticket);

        if (oldStatus != newStatus && (oldStatus == Ticket.Status.CLOSED || newStatus == Ticket.Status.CLOSED)) {
            recalculateAllPriorities();
        }

        User updater = userRepository.findByUsername(updaterUsername).orElseThrow();
        logHistory(ticket, updater, "STATUS_CHANGED",
                "Статус изменен с " + oldStatus.getDisplayName() + " на " + newStatus.getDisplayName());

        return ticket;
    }

    @Transactional
    public Ticket assignTicket(Integer ticketId, String executorUsername, String assignerUsername) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        User executor = userRepository.findByUsername(executorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Executor user not found: " + executorUsername));
        ticket.setExecutor(executor);
        if (ticket.getStatus() == Ticket.Status.NEW) {
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
        }
        ticket = ticketRepository.save(ticket);

        User assigner = userRepository.findByUsername(assignerUsername).orElseThrow();
        logHistory(ticket, assigner, "ASSIGNED", "Назначена исполнителю: " + executorUsername);
        return ticket;
    }

    @Transactional
    public Ticket unassignTicket(Integer ticketId, String updaterUsername) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        ticket.setExecutor(null);
        if (ticket.getStatus() == Ticket.Status.IN_PROGRESS) {
            ticket.setStatus(Ticket.Status.NEW);
        }
        ticket = ticketRepository.save(ticket);

        User updater = userRepository.findByUsername(updaterUsername).orElseThrow();
        logHistory(ticket, updater, "UNASSIGNED", "Исполнитель снят");
        return ticket;
    }

    @Transactional
    public Optional<Ticket> autoAssignHighestPriorityTicket(String executorUsername) {
        User executor = userRepository.findByUsername(executorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Executor user not found: " + executorUsername));

        // Find all unassigned, unresolved tickets sorted by priority (highest first)
        List<Ticket> unassignedTickets = ticketRepository.findByExecutorIsNullAndStatusNot(Ticket.Status.CLOSED,
                Sort.by("priorityScore").descending());

        if (unassignedTickets.isEmpty()) {
            return Optional.empty();
        }

        // Assign the first (highest priority) ticket
        Ticket ticket = unassignedTickets.get(0);
        ticket.setExecutor(executor);
        if (ticket.getStatus() == Ticket.Status.NEW) {
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
        }
        ticket = ticketRepository.save(ticket);

        logHistory(ticket, executor, "ASSIGNED", "Автоматически назначена как наиболее приоритетная");
        return Optional.of(ticket);
    }

    @Transactional
    public void deleteTicket(Integer id) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            boolean wasActive = ticket.getStatus() != Ticket.Status.CLOSED;
            ticketRepository.deleteById(id);
            if (wasActive) {
                recalculateAllPriorities();
            }
        }
    }

    @Transactional
    public void recalculateAllPriorities() {
        List<Ticket> activeTickets = ticketRepository.findAll();
        for (Ticket ticket : activeTickets) {
            if (ticket.getStatus() != Ticket.Status.CLOSED) {
                ticket.setPriorityScore(calculatePriorityScore(ticket));
                ticketRepository.save(ticket);
            }
        }
    }

    public Optional<Ticket> getRecommendedTicket() {
        return ticketRepository.findFirstByStatusNotAndExecutorIsNullOrderByPriorityScoreDesc(Ticket.Status.CLOSED);
    }

    @Transactional(readOnly = true)
    public List<TicketComment> getComments(Ticket ticket) {
        // Fetch comments with their attachments proactively to prevent
        // LazyInitializationException or empty collections in Thymeleaf
        List<TicketComment> comments = ticketCommentRepository.findByTicketWithAttachmentsOrderByCreatedAtAsc(ticket);
        // Ensure collections are uniformly deduplicated or initialized, though left
        // join fetch returns a unique root entity if we use Set, for Lists it may
        // duplicate if multiple attachments.
        // It's a OneToMany so List with JOIN FETCH could multiply the comment instance.
        // We must distinct it.
        return comments.stream().distinct().collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> getHistory(Ticket ticket) {
        return ticketHistoryRepository.findByTicketOrderByCreatedAtDesc(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketAttachment> getAttachments(Ticket ticket) {
        return ticketAttachmentRepository.findByTicketAndCommentIsNullOrderByCreatedAtAsc(ticket);
    }

    @Transactional(readOnly = true)
    public Optional<TicketAttachment> getAttachment(Integer id) {
        return ticketAttachmentRepository.findById(id);
    }

    @Transactional
    public TicketAttachment addAttachment(Integer ticketId, org.springframework.web.multipart.MultipartFile file,
            String uploaderUsername) throws java.io.IOException {
        return addAttachmentInternal(ticketId, file, uploaderUsername, null);
    }

    @Transactional
    public TicketAttachment addCommentAttachment(Integer ticketId, org.springframework.web.multipart.MultipartFile file,
            String uploaderUsername, TicketComment comment) throws java.io.IOException {
        return addAttachmentInternal(ticketId, file, uploaderUsername, comment);
    }

    private TicketAttachment addAttachmentInternal(Integer ticketId,
            org.springframework.web.multipart.MultipartFile file, String uploaderUsername, TicketComment comment)
            throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран.");
        }
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + ticketId));

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(ticket);
        attachment.setComment(comment);
        attachment.setFileName(org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename()));
        String contentType = file.getContentType();
        if (contentType == null || contentType.equals("application/octet-stream")) {
            contentType = MediaTypeFactory.getMediaType(attachment.getFileName())
                    .map(org.springframework.http.MediaType::toString)
                    .orElse("application/octet-stream");
        }
        attachment.setFileType(contentType);
        attachment.setData(file.getBytes());
        User uploader = userRepository.findByUsername(uploaderUsername).orElseThrow();
        attachment.setUploader(uploader);

        ticketAttachmentRepository.save(attachment);

        logHistory(ticket, uploader, "ATTACHMENT_ADDED", "Добавлен файл: " + attachment.getFileName());

        return attachment;
    }

    @Transactional
    public void deleteAttachment(Integer attachmentId, String username) {
        TicketAttachment attachment = ticketAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Вложение не найдено: " + attachmentId));

        User user = userRepository.findByUsername(username).orElseThrow();
        Ticket ticket = attachment.getTicket();

        // Permission check: Uploader, Support or Admin
        boolean isUploader = attachment.getUploader() != null
                && attachment.getUploader().getUsername().equals(username);
        boolean isSupportOrAdmin = user.getRole().getName().equals("ROLE_IT_SUPPORT")
                || user.getRole().getName().equals("ROLE_ADMIN");

        if (!isUploader && !isSupportOrAdmin) {
            throw new IllegalArgumentException("У вас нет прав для удаления этого вложения.");
        }

        String fileName = attachment.getFileName();
        ticketAttachmentRepository.delete(attachment);
        logHistory(ticket, user, "ATTACHMENT_DELETED", "Удален файл: " + fileName);
    }

    @Transactional
    public TicketComment addComment(Integer ticketId, String authorUsername, String text) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Author not found: " + authorUsername));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setText(text);

        logHistory(ticket, author, "COMMENT_ADDED", "Добавлен комментарий");
        return ticketCommentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalTickets(ticketRepository.count());
        long closedTickets = ticketRepository.countByStatus(Ticket.Status.CLOSED);
        stats.setClosedTickets(closedTickets);
        stats.setOpenTickets(stats.getTotalTickets() - closedTickets);

        long overdue = ticketRepository.countBySlaDeadlineBeforeAndStatusNot(LocalDateTime.now(), Ticket.Status.CLOSED);
        stats.setOverdueTickets(overdue);

        List<Ticket> closedList = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == Ticket.Status.CLOSED && t.getClosedAt() != null)
                .toList();

        if (!closedList.isEmpty()) {
            double avg = closedList.stream()
                    .mapToLong(t -> java.time.Duration.between(t.getCreatedAt(), t.getClosedAt()).toHours())
                    .average()
                    .orElse(0.0);
            stats.setAvgResolutionTimeHours(avg);
        } else {
            stats.setAvgResolutionTimeHours(0.0);
        }

        stats.setStatusDistribution(ticketRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getStatus().name(),
                        java.util.stream.Collectors.counting())));

        stats.setCategoryDistribution(ticketRepository.findAll().stream()
                .filter(t -> t.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getCategory().name(),
                        java.util.stream.Collectors.counting())));

        return stats;
    }

    private void logHistory(Ticket ticket, User user, String action, String description) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setUser(user);
        history.setAction(action);
        history.setDescription(description);
        ticketHistoryRepository.save(history);
    }

    // Метод для расчета priorityScore
    public double calculatePriorityScore(Ticket ticket) {
        double score = 0.0;

        // Важность
        Optional<PriorityWeight> importanceWeight = priorityWeightRepository
                .findByParamName(PriorityParameter.IMPORTANCE);
        if (importanceWeight.isPresent() && !Boolean.FALSE.equals(importanceWeight.get().getActive())
                && ticket.getImportance() != null) {
            score += importanceWeight.get().getWeightValue() * (ticket.getImportance().ordinal() + 1);
        }

        // Кол-во новых заявок
        Optional<PriorityWeight> newerUnresolvedWeight = priorityWeightRepository
                .findByParamName(PriorityParameter.NEWER_UNRESOLVED_TICKETS);
        if (newerUnresolvedWeight.isPresent() && !Boolean.FALSE.equals(newerUnresolvedWeight.get().getActive())
                && ticket.getCreatedAt() != null) {
            long count = ticketRepository.countByStatusNotAndCreatedAtAfter(Ticket.Status.CLOSED,
                    ticket.getCreatedAt());
            score += newerUnresolvedWeight.get().getWeightValue() * count;
        }

        // Влияние
        Optional<PriorityWeight> impactWeight = priorityWeightRepository.findByParamName(PriorityParameter.IMPACT);
        if (impactWeight.isPresent() && !Boolean.FALSE.equals(impactWeight.get().getActive())
                && ticket.getImpact() != null) {
            score += impactWeight.get().getWeightValue() * (ticket.getImpact().ordinal() + 1);
        }

        // Срочность
        Optional<PriorityWeight> urgencyWeight = priorityWeightRepository.findByParamName(PriorityParameter.URGENCY);
        if (urgencyWeight.isPresent() && !Boolean.FALSE.equals(urgencyWeight.get().getActive())
                && ticket.getUrgency() != null) {
            score += urgencyWeight.get().getWeightValue() * (ticket.getUrgency().ordinal() + 1);
        }

        // Категория
        Optional<PriorityWeight> categoryWeight = priorityWeightRepository.findByParamName(PriorityParameter.CATEGORY);
        if (categoryWeight.isPresent() && !Boolean.FALSE.equals(categoryWeight.get().getActive())
                && ticket.getCategory() != null) {
            score += categoryWeight.get().getWeightValue() * (ticket.getCategory().ordinal() + 1);
        }

        // Значимость роли
        Optional<PriorityWeight> roleWeight = priorityWeightRepository.findByParamName(PriorityParameter.CREATOR_ROLE);
        if (roleWeight.isPresent() && !Boolean.FALSE.equals(roleWeight.get().getActive()) && ticket.getCreator() != null
                && ticket.getCreator().getRole() != null) {
            String roleName = ticket.getCreator().getRole().getName();
            if ("ROLE_VIP".equals(roleName)) {
                score += roleWeight.get().getWeightValue();
            } else if ("ROLE_ADMIN".equals(roleName)) {
                score += roleWeight.get().getWeightValue() * 0.5; // Слегка повышаем
            }
        }

        // Время ожидания
        Optional<PriorityWeight> waitingWeight = priorityWeightRepository
                .findByParamName(PriorityParameter.WAITING_HOURS);
        if (waitingWeight.isPresent() && !Boolean.FALSE.equals(waitingWeight.get().getActive())
                && ticket.getCreatedAt() != null) {
            long hours = java.time.Duration.between(ticket.getCreatedAt(), LocalDateTime.now()).toHours();
            score += waitingWeight.get().getWeightValue() * hours;
        }

        // Динамические параметры
        if (ticket.getDynamicValues() != null) {
            for (TicketDynamicValue dv : ticket.getDynamicValues()) {
                if (dv.getFilter() != null && !Boolean.FALSE.equals(dv.getFilter().getIsActive())
                        && dv.getValue() != null) {
                    score += dv.getFilter().getWeight() * dv.getValue().getWeightValue();
                }
            }
        }

        return score;
    }
}

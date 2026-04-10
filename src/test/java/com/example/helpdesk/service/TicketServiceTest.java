package com.example.helpdesk.service;

import com.example.helpdesk.domain.*;
import com.example.helpdesk.dto.DashboardStats;
import com.example.helpdesk.repository.PriorityWeightRepository;
import com.example.helpdesk.repository.TicketAttachmentRepository;
import com.example.helpdesk.repository.TicketCommentRepository;
import com.example.helpdesk.repository.TicketHistoryRepository;
import com.example.helpdesk.repository.TicketRepository;
import com.example.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PriorityWeightRepository priorityWeightRepository;
    @Mock
    private TicketHistoryRepository ticketHistoryRepository;
    @Mock
    private TicketCommentRepository ticketCommentRepository;
    @Mock
    private TicketAttachmentRepository ticketAttachmentRepository;
    @Mock
    private MultipartFile multipartFile;

    private TicketService ticketService;

    private User testUser;
    private User testExecutor;
    private Role userRole;
    private Role adminRole;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                ticketRepository,
                userRepository,
                priorityWeightRepository,
                ticketHistoryRepository,
                ticketCommentRepository,
                ticketAttachmentRepository
        );

        userRole = new Role();
        userRole.setId(1);
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setRole(userRole);

        testExecutor = new User();
        testExecutor.setId(2);
        testExecutor.setUsername("executor");
        testExecutor.setFullName("Executor User");
        testExecutor.setRole(adminRole);

        testTicket = new Ticket();
        testTicket.setId(1);
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setCreator(testUser);
        testTicket.setStatus(Ticket.Status.NEW);
        testTicket.setImportance(Importance.HIGH);
        testTicket.setUrgency(Urgency.HIGH);
        testTicket.setImpact(Impact.DEPARTMENT);
        testTicket.setCategory(Category.SOFTWARE);
        testTicket.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createTicket_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(priorityWeightRepository.findByParamName(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(1);
            }
            return t;
        });
        when(ticketRepository.findAll()).thenReturn(List.of());

        Ticket result = ticketService.createTicket(
                "Test Ticket",
                "Test Description",
                "testuser",
                Importance.HIGH,
                Urgency.HIGH,
                Impact.DEPARTMENT,
                Category.SOFTWARE
        );

        assertNotNull(result);
        assertEquals("Test Ticket", result.getTitle());
        assertEquals(Ticket.Status.NEW, result.getStatus());
        assertEquals(testUser, result.getCreator());
        verify(ticketRepository, atLeast(2)).save(any(Ticket.class));
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void createTicket_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                ticketService.createTicket(
                        "Test",
                        "Desc",
                        "nonexistent",
                        Importance.HIGH,
                        Urgency.MEDIUM,
                        Impact.USER,
                        Category.SOFTWARE
                )
        );
    }

    @Test
    void createTicket_SetsCorrectSlaDeadline_ForCriticalUrgency() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(priorityWeightRepository.findByParamName(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(1);
            }
            return t;
        });
        when(ticketRepository.findAll()).thenReturn(List.of());

        Ticket result = ticketService.createTicket(
                "Test",
                "Desc",
                "testuser",
                Importance.HIGH,
                Urgency.CRITICAL,
                Impact.DEPARTMENT,
                Category.SOFTWARE
        );

        assertNotNull(result.getSlaDeadline());
        assertEquals(4, java.time.Duration.between(result.getCreatedAt(), result.getSlaDeadline()).toHours());
    }

    @Test
    void findTicketById_ReturnsTicket_WhenExists() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));

        Optional<Ticket> result = ticketService.findTicketById(1);

        assertTrue(result.isPresent());
        assertEquals(testTicket, result.get());
    }

    @Test
    void findTicketById_ReturnsEmpty_WhenNotExists() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Ticket> result = ticketService.findTicketById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void updateTicketStatus_ToClosed_Success() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketRepository.findAll()).thenReturn(List.of());

        Ticket result = ticketService.updateTicketStatus(1, Ticket.Status.CLOSED, "testuser", "Problem solved");

        assertEquals(Ticket.Status.CLOSED, result.getStatus());
        assertEquals("Problem solved", result.getResolution());
        assertNotNull(result.getClosedAt());
    }

    @Test
    void updateTicketStatus_ToClosed_WithoutResolution_ThrowsException() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                ticketService.updateTicketStatus(1, Ticket.Status.CLOSED, "testuser", null)
        );
    }

    @Test
    void updateTicketStatus_ToClosed_EmptyResolution_ThrowsException() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalArgumentException.class, () ->
                ticketService.updateTicketStatus(1, Ticket.Status.CLOSED, "testuser", "   ")
        );
    }

    @Test
    void assignTicket_Success() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("executor")).thenReturn(Optional.of(testExecutor));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        Ticket result = ticketService.assignTicket(1, "executor", "testuser");

        assertEquals(testExecutor, testTicket.getExecutor());
        assertEquals(Ticket.Status.IN_PROGRESS, testTicket.getStatus());
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void assignTicket_TicketNotFound_ThrowsException() {
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                ticketService.assignTicket(999, "executor", "testuser")
        );
    }

    @Test
    void assignTicket_ExecutorNotFound_ThrowsException() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                ticketService.assignTicket(1, "nonexistent", "testuser")
        );
    }

    @Test
    void unassignTicket_Success() {
        testTicket.setExecutor(testExecutor);
        testTicket.setStatus(Ticket.Status.IN_PROGRESS);

        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        Ticket result = ticketService.unassignTicket(1, "testuser");

        assertNull(testTicket.getExecutor());
        assertEquals(Ticket.Status.NEW, testTicket.getStatus());
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void deleteTicket_Success() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.findAll()).thenReturn(List.of());

        ticketService.deleteTicket(1);

        verify(ticketRepository).deleteById(1);
    }

    @Test
    void deleteAttachment_ByUploader_Success() {
        TicketAttachment attachment = new TicketAttachment();
        attachment.setId(1);
        attachment.setTicket(testTicket);
        attachment.setUploader(testUser);
        attachment.setFileName("test.txt");

        when(ticketAttachmentRepository.findById(1)).thenReturn(Optional.of(attachment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ticketService.deleteAttachment(1, "testuser");

        verify(ticketAttachmentRepository).delete(attachment);
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void deleteAttachment_ByAdmin_Success() {
        TicketAttachment attachment = new TicketAttachment();
        attachment.setId(1);
        attachment.setTicket(testTicket);
        attachment.setUploader(testUser);
        attachment.setFileName("test.txt");

        when(ticketAttachmentRepository.findById(1)).thenReturn(Optional.of(attachment));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testExecutor));

        ticketService.deleteAttachment(1, "admin");

        verify(ticketAttachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_WithoutPermission_ThrowsException() {
        User anotherUser = new User();
        anotherUser.setId(3);
        anotherUser.setUsername("another");
        anotherUser.setRole(userRole);

        TicketAttachment attachment = new TicketAttachment();
        attachment.setId(1);
        attachment.setTicket(testTicket);
        attachment.setUploader(testUser);
        attachment.setFileName("test.txt");

        when(ticketAttachmentRepository.findById(1)).thenReturn(Optional.of(attachment));
        when(userRepository.findByUsername("another")).thenReturn(Optional.of(anotherUser));

        assertThrows(IllegalArgumentException.class, () ->
                ticketService.deleteAttachment(1, "another")
        );
    }

    @Test
    void addComment_Success() {
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(invocation -> {
            TicketComment c = invocation.getArgument(0);
            c.setId(1);
            return c;
        });

        TicketComment result = ticketService.addComment(1, "testuser", "Test comment");

        assertNotNull(result);
        assertEquals("Test comment", result.getText());
        assertEquals(testTicket, result.getTicket());
        assertEquals(testUser, result.getAuthor());
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void addAttachment_EmptyFile_ThrowsException() {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                ticketService.addAttachment(1, multipartFile, "testuser")
        );
    }

    @Test
    void addAttachment_Success() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getBytes()).thenReturn("test content".getBytes());
        when(ticketRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(ticketAttachmentRepository.save(any(TicketAttachment.class))).thenAnswer(invocation -> {
            TicketAttachment a = invocation.getArgument(0);
            a.setId(1);
            return a;
        });

        TicketAttachment result = ticketService.addAttachment(1, multipartFile, "testuser");

        assertNotNull(result);
        assertEquals("test.txt", result.getFileName());
        assertEquals("text/plain", result.getFileType());
        verify(ticketHistoryRepository).save(any(TicketHistory.class));
    }

    @Test
    void getDashboardStats_ReturnsCorrectStats() {
        Ticket closedTicket = new Ticket();
        closedTicket.setId(2);
        closedTicket.setStatus(Ticket.Status.CLOSED);
        closedTicket.setCreatedAt(LocalDateTime.now().minusHours(10));
        closedTicket.setClosedAt(LocalDateTime.now());

        when(ticketRepository.count()).thenReturn(3L);
        when(ticketRepository.countByStatus(Ticket.Status.CLOSED)).thenReturn(1L);
        when(ticketRepository.countBySlaDeadlineBeforeAndStatusNot(any(), eq(Ticket.Status.CLOSED))).thenReturn(0L);
        when(ticketRepository.findAll()).thenReturn(List.of(testTicket, closedTicket));

        DashboardStats stats = ticketService.getDashboardStats();

        assertEquals(3L, stats.getTotalTickets());
        assertEquals(1L, stats.getClosedTickets());
        assertEquals(2L, stats.getOpenTickets());
        assertEquals(0L, stats.getOverdueTickets());
        assertTrue(stats.getAvgResolutionTimeHours() > 0);
    }

    @Test
    void calculatePriorityScore_WithAllParameters() {
        PriorityWeight importanceWeight = new PriorityWeight();
        importanceWeight.setWeightValue(2.0);

        PriorityWeight impactWeight = new PriorityWeight();
        impactWeight.setWeightValue(1.5);

        PriorityWeight urgencyWeight = new PriorityWeight();
        urgencyWeight.setWeightValue(3.0);

        when(priorityWeightRepository.findByParamName(PriorityParameter.IMPORTANCE)).thenReturn(Optional.of(importanceWeight));
        when(priorityWeightRepository.findByParamName(PriorityParameter.IMPACT)).thenReturn(Optional.of(impactWeight));
        when(priorityWeightRepository.findByParamName(PriorityParameter.URGENCY)).thenReturn(Optional.of(urgencyWeight));
        when(priorityWeightRepository.findByParamName(PriorityParameter.NEWER_UNRESOLVED_TICKETS)).thenReturn(Optional.empty());
        when(priorityWeightRepository.findByParamName(PriorityParameter.CATEGORY)).thenReturn(Optional.empty());
        when(priorityWeightRepository.findByParamName(PriorityParameter.CREATOR_ROLE)).thenReturn(Optional.empty());
        when(priorityWeightRepository.findByParamName(PriorityParameter.WAITING_HOURS)).thenReturn(Optional.empty());

        double score = ticketService.calculatePriorityScore(testTicket);

        assertTrue(score > 0);
        assertEquals(18.0, score, 0.01);
    }

    @Test
    void findTicketsByExecutorUsername_EmptyUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                ticketService.findTicketsByExecutorUsername("", false, Sort.unsorted())
        );
    }

    @Test
    void findTicketsByExecutorUsername_BlankUsername_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                ticketService.findTicketsByExecutorUsername("   ", false, Sort.unsorted())
        );
    }

    @Test
    void getRecommendedTicket_ReturnsHighestPriority() {
        Ticket highPriority = new Ticket();
        highPriority.setId(1);
        highPriority.setPriorityScore(100.0);

        Ticket lowPriority = new Ticket();
        lowPriority.setId(2);
        lowPriority.setPriorityScore(50.0);

        when(ticketRepository.findFirstByStatusNotAndExecutorIsNullOrderByPriorityScoreDesc(Ticket.Status.CLOSED))
                .thenReturn(Optional.of(highPriority));

        Optional<Ticket> result = ticketService.getRecommendedTicket();

        assertTrue(result.isPresent());
        assertEquals(highPriority, result.get());
    }

    @Test
    void autoAssignHighestPriorityTicket_Success() {
        when(userRepository.findByUsername("executor")).thenReturn(Optional.of(testExecutor));
        when(ticketRepository.findByExecutorIsNullAndStatusNot(eq(Ticket.Status.CLOSED), any(Sort.class)))
                .thenReturn(List.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        Optional<Ticket> result = ticketService.autoAssignHighestPriorityTicket("executor");

        assertTrue(result.isPresent());
        assertEquals(testExecutor, testTicket.getExecutor());
        assertEquals(Ticket.Status.IN_PROGRESS, testTicket.getStatus());
    }

    @Test
    void autoAssignHighestPriorityTicket_NoTickets_ReturnsEmpty() {
        when(userRepository.findByUsername("executor")).thenReturn(Optional.of(testExecutor));
        when(ticketRepository.findByExecutorIsNullAndStatusNot(eq(Ticket.Status.CLOSED), any(Sort.class)))
                .thenReturn(List.of());

        Optional<Ticket> result = ticketService.autoAssignHighestPriorityTicket("executor");

        assertFalse(result.isPresent());
    }
}

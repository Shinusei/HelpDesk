package com.example.helpdesk.service;

import com.example.helpdesk.domain.Importance;
import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.PriorityWeightRepository; // Импортируем
import com.example.helpdesk.repository.TicketRepository;
import com.example.helpdesk.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PriorityWeightRepository priorityWeightRepository; // Добавляем

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, PriorityWeightRepository priorityWeightRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.priorityWeightRepository = priorityWeightRepository;
    }

    @Transactional
    public Ticket createTicket(String title, String description, String creatorUsername, Importance importance) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator user not found: " + creatorUsername));

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCreator(creator);
        ticket.setStatus(Ticket.Status.NEW);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setImportance(importance); // Устанавливаем важность

        // Расчет priorityScore
        double score = calculatePriorityScore(importance);
        ticket.setPriorityScore(score);

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> findTicketById(Integer id) {
        return ticketRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByCreator(String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Creator user not found: " + creatorUsername));
        return ticketRepository.findByCreator(creator);
    }

    @Transactional
    public Ticket updateTicketStatus(Integer ticketId, Ticket.Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignTicket(Integer ticketId, String executorUsername) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        User executor = userRepository.findByUsername(executorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Executor user not found: " + executorUsername));
        ticket.setExecutor(executor);
        if (ticket.getStatus() == Ticket.Status.NEW) {
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket unassignTicket(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        ticket.setExecutor(null);
        if (ticket.getStatus() == Ticket.Status.IN_PROGRESS) {
            ticket.setStatus(Ticket.Status.NEW);
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteTicket(Integer id) {
        ticketRepository.deleteById(id);
    }

    // Метод для расчета priorityScore
    private double calculatePriorityScore(Importance importance) {
        double score = 0.0;

        // Получаем вес для параметра "Важность"
        Optional<PriorityWeight> importanceWeight = priorityWeightRepository.findByParamName(PriorityParameter.IMPORTANCE);
        if (importanceWeight.isPresent()) {
            double weight = importanceWeight.get().getWeightValue();
            // Пример простой формулы: вес * (порядковый номер важности)
            // LOW=1, MEDIUM=2, HIGH=3
            score += weight * (importance.ordinal() + 1);
        }

        // Здесь можно добавить логику для других параметров, если они будут
        // Например, если будет URGENCY, то:
        // Optional<PriorityWeight> urgencyWeight = priorityWeightRepository.findByParamName(PriorityParameter.URGENCY);
        // if (urgencyWeight.isPresent()) {
        //     score += urgencyWeight.get().getWeightValue() * urgency.ordinal();
        // }

        return score;
    }
}

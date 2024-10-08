package back.service;

import back.controller.dto.CashflowRecordDTO;
import back.model.CashflowRecord;
import back.model.Category;
import back.model.Sharing;
import back.model.User;
import back.repository.CashflowRecordRepository;
import back.repository.CategoryRepository;
import back.repository.SharingRepository;
import back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashflowRecordService {

    private final CashflowRecordRepository cashflowRecordRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SharingRepository sharingRepository;

    public List<CashflowRecordDTO> getCashflowRecordsByUserId(Long userId) {
        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return cashflowRecordRepository.findRecordsByUserId(userId);
    }

    public CashflowRecordDTO addCashflowRecord(CashflowRecordDTO cashflowRecordDTO, Long userId) {
//        // Sprawdzenie, czy userId nie jest null
//        if (userId == null) {
//            throw new IllegalArgumentException("User ID must not be null");
//        }

        // Pobranie użytkownika
        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Sprawdzenie warunków na podstawie recordType
        if (cashflowRecordDTO.isRecordType() && cashflowRecordDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID must not be null when recordType is true");
        }

        if (!cashflowRecordDTO.isRecordType() && cashflowRecordDTO.getCategoryId() != null) {
            throw new IllegalArgumentException("Category ID must be null when recordType is false");
        }

        // Pobranie kategorii, jeśli categoryId jest podane
        Category category = null;
        if (cashflowRecordDTO.getCategoryId() != null) {
            category = categoryRepository.findById(cashflowRecordDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + cashflowRecordDTO.getCategoryId()));
        }

        // Tworzenie nowego CashflowRecord
        CashflowRecord cashflowRecord = new CashflowRecord();
        cashflowRecord.setAmount(cashflowRecordDTO.getAmount());
        cashflowRecord.setDate(cashflowRecordDTO.getDate());
        cashflowRecord.setRecordType(cashflowRecordDTO.isRecordType());
        cashflowRecord.setCategory(category);

        // Zapis rekordu
        CashflowRecord savedCashflowRecord = cashflowRecordRepository.save(cashflowRecord);

        // Tworzenie i zapisywanie rekordu Sharing
        Sharing sharing = new Sharing();
        sharing.setCashflowRecord(savedCashflowRecord);
        sharing.setUser(user);
        sharingRepository.save(sharing);

        // Zwrot DTO z zapisanego rekordu
        return new CashflowRecordDTO(savedCashflowRecord.getAmount(), savedCashflowRecord.getDate(), savedCashflowRecord.isRecordType(),
                savedCashflowRecord.getCategory() != null ? savedCashflowRecord.getCategory().getCategoryId() : null, userId);
    }

    public CashflowRecordDTO updateCashflowRecord(Long recordId, CashflowRecordDTO newRecordData) {
        // Pobierz rekord CashflowRecord
        CashflowRecord existingRecord = cashflowRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with id: " + recordId));

        Category category = categoryRepository.findById(newRecordData.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + newRecordData.getCategoryId()));

//        // nie wiem czu tu nie działa
//        if (newRecordData.isRecordType() && newRecordData.getCategoryId() == null) {
//            throw new IllegalArgumentException("Category ID must not be null when recordType is true");
//        }
//
//        if (!newRecordData.isRecordType() && newRecordData.getCategoryId() != null) {
//            throw new IllegalArgumentException("Category ID must be null when recordType is false");
//        }

        // Zaktualizuj pola rekordu
        existingRecord.setAmount(newRecordData.getAmount());
        existingRecord.setDate(newRecordData.getDate());
        existingRecord.setRecordType(newRecordData.isRecordType());
        existingRecord.setCategory(category);

        // Zapisz zaktualizowany rekord
        CashflowRecord updatedRecord = cashflowRecordRepository.save(existingRecord);

        // Pobierz powiązany rekord Sharing, aby uzyskać userId
        Sharing sharing = sharingRepository.findByCashflowRecord_CashflowRecordId(updatedRecord.getCashflowRecordId())
                .orElseThrow(() -> new IllegalArgumentException("No sharing record found for cashflow record id: " + updatedRecord.getCashflowRecordId()));

        Long userId = sharing.getUser().getUserId();

        return new CashflowRecordDTO(
                updatedRecord.getAmount(),
                updatedRecord.getDate(),
                updatedRecord.isRecordType(),
                updatedRecord.getCategory().getCategoryId(),
                updatedRecord.getCategory().getTitle(),
                userId
        );
    }

//    public void deleteCashflowRecord(Long userId, Long recordId) {
//        CashflowRecord cashflowRecord = cashflowRecordRepository.findByCashflowRecordIdAndUserId(recordId, userId)
//                .orElseThrow(() -> new IllegalArgumentException("Cashflow record not found for this user"));
//        cashflowRecordRepository.delete(cashflowRecord);
//    }
}
package com.home.service.models;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.home.service.dto.District;

@Service
public class DistrictService {
    private final List<District> districts = List.of(
            new District("Addis Ketema", "አዲስ ከተማ", "Addis Ketemaa", 14),
            new District("Akaki Kaliti", "አቃቂ ቃሊቲ", "Akakii Kaalitii", 13),
            new District("Arada", "አራዳ", "Aradaa", 10),
            new District("Bole", "ቦሌ", "Bolee", 15),
            new District("Gullele", "ጉለሌ", "Gulleelee", 10),
            new District("Kirkos", "ቂርቆስ", "Kirkos", 11),
            new District("Kolfe Keranio", "ኮልፌ ቀራኒዮ", "Kolfe Keranio", 14),
            new District("Ledeta", "ልደታ", "Ledeta jedhamtu", 10),
            new District("Nifas Silk Lafto", "ንፋስ ስልክ ላፍቶ", "Nifaas Siilkii Laaftoo", 15),
            new District("Yeka", "የካ", "Yekaa", 13),
            new District("Lemi Kura", "ለሚ ኩራ", "Leemii Kuraa", 15));

    public List<District> getDistricts(Optional<String> language, Optional<String> query) {
        return districts.stream()
                .filter(d -> query.map(q -> matchesQuery(d, language.orElse("english"), q)).orElse(true))
                .toList();
    }

    private boolean matchesQuery(District d, String language, String query) {
        return switch (language.toLowerCase()) {
            case "amharic" -> d.getAmharicName().contains(query);
            case "oromo" -> d.getOromoName().contains(query);
            default -> d.getEnglishName().contains(query);
        };
    }
}

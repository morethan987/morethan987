package com.example.GradeSystemBackend.dto;

public class CardDataDTO {

    private String id;
    private String title;
    private String value;
    private TrendDTO trend;
    private FooterDTO footer;

    public CardDataDTO() {}

    public CardDataDTO(
        String id,
        String title,
        String value,
        TrendDTO trend,
        FooterDTO footer
    ) {
        this.id = id;
        this.title = title;
        this.value = value;
        this.trend = trend;
        this.footer = footer;
    }

    public static CardDataDTO create(
        String id,
        String title,
        String value,
        String trendDirection,
        String trendValue,
        boolean trendVisible,
        String footerStatus,
        String footerDescription
    ) {
        return new CardDataDTO(
            id,
            title,
            value,
            new TrendDTO(trendDirection, trendValue, trendVisible),
            new FooterDTO(footerStatus, footerDescription)
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TrendDTO getTrend() {
        return trend;
    }

    public void setTrend(TrendDTO trend) {
        this.trend = trend;
    }

    public FooterDTO getFooter() {
        return footer;
    }

    public void setFooter(FooterDTO footer) {
        this.footer = footer;
    }

    public static class TrendDTO {
        private String direction;
        private String value;
        private Boolean isVisible;

        public TrendDTO() {}

        public TrendDTO(String direction, String value, Boolean isVisible) {
            this.direction = direction;
            this.value = value;
            this.isVisible = isVisible;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean getIsVisible() {
            return isVisible;
        }

        public void setIsVisible(Boolean isVisible) {
            this.isVisible = isVisible;
        }
    }

    public static class FooterDTO {
        private String status;
        private String description;

        public FooterDTO() {}

        public FooterDTO(String status, String description) {
            this.status = status;
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

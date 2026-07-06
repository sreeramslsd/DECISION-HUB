package com.decisionhub.mapper;

import com.decisionhub.dto.ReportRequest;
import com.decisionhub.dto.ReportResponse;
import com.decisionhub.entity.Report;
import com.decisionhub.entity.ReportFormat;
import com.decisionhub.entity.User;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-05T11:41:10+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ReportMapperImpl implements ReportMapper {

    @Override
    public ReportResponse toResponse(Report report) {
        if ( report == null ) {
            return null;
        }

        UUID userId = null;
        UUID id = null;
        String title = null;
        String fileUrl = null;
        ReportFormat format = null;
        long sizeBytes = 0L;
        Instant createdAt = null;

        userId = reportUserId( report );
        id = report.getId();
        title = report.getTitle();
        fileUrl = report.getFileUrl();
        format = report.getFormat();
        sizeBytes = report.getSizeBytes();
        createdAt = report.getCreatedAt();

        ReportResponse reportResponse = new ReportResponse( id, userId, title, fileUrl, format, sizeBytes, createdAt );

        return reportResponse;
    }

    @Override
    public Report toEntity(ReportRequest request) {
        if ( request == null ) {
            return null;
        }

        Report report = new Report();

        report.setTitle( request.title() );
        report.setFormat( request.format() );

        return report;
    }

    private UUID reportUserId(Report report) {
        User user = report.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }
}

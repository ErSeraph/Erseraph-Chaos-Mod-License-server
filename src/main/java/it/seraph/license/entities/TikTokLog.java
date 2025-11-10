package it.seraph.license.entities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tik_tok_log")
@Data
public class TikTokLog {

	public TikTokLog(Users user, Long liveId, Long timestamp, Long diamanti) {
		super();
		this.user = user;
		this.liveId = liveId;
		this.timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
		this.diamanti = diamanti;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@JoinColumn(name = "user_id", nullable = false)
	@ManyToOne
	private Users user;
	
	@Column(name = "liveId", nullable = false)
    private Long liveId;
	
	@Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;
	
	@Column(name = "diamanti", nullable = false)
    private Long diamanti;
}

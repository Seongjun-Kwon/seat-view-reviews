package com.goodseats.seatviewreviews.domain.vote.repository;

public interface VoteTypeRepository {

	boolean existsByReferenceId(Long referenceId);
}

package com.rakuten.felix.testsend.manager.datastore;

import com.rakuten.felix.testsend.manager.datastore.entities.TestSendHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TestSendHistoryRepository extends JpaRepository<TestSendHistory, Integer> {

    Optional<TestSendHistory> findById(Integer id);

    Optional<TestSendHistory> findByJobId(Integer jobId);

    Page<TestSendHistory> findByBundleIdAndBundleType(Integer bundleId, Integer bundleType, Pageable pageable);

    /**
     * Update job id by
     *
     * @param id    History id.
     * @param jobId Job id
     * @return Number of rows affected.
     */
    @Modifying
    @Query("update TestSendHistory t set t.jobId = :jobId where t.id = :id")
    int updateJobId(@Param("id") Integer id, @Param("jobId") Integer jobId);

    /**
     * Update info and status to finished
     *
     * @param jobId Job id.
     * @param info  Info json string.
     * @return Number of rows affected.
     */
    @Modifying
    @Query("update TestSendHistory t set t.info = :info, t.status = com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus.FINISHED where t.jobId = :jobId")
    int updateInfoAndStatusFinished(Integer jobId, String info);

    /**
     * Update info and status to error
     *
     * @param jobId Job id.
     * @param info  Info json string.
     * @return Number of rows affected.
     */
    @Modifying
    @Query("update TestSendHistory t set t.info = :info, t.status = com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus.ERROR where t.jobId = :jobId")
    int updateInfoAndStatusError(Integer jobId, String info);

    /**
     * Update status to error
     *
     * @param jobId Job id.
     * @return Number of rows affected.
     */
    @Modifying
    @Query("update TestSendHistory t set t.status = com.rakuten.felix.testsend.manager.datastore.entities.TestSendStatus.ERROR where t.jobId = :jobId")
    int updateStatusError(Integer jobId);
}

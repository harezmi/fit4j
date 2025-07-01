package com.udemy.acceptancetests.examples.coroutines

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.services.dto.user.UserOuterClass.User
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceGrpc
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUsersRequest
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUsersResponse
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer
import net.devh.boot.grpc.server.service.GrpcService
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.test.context.TestPropertySource
import java.util.Date
import java.util.concurrent.Executors

@AcceptanceTest
@TestPropertySource(properties = ["withoutCoroutines=true"])
class GrpcWithoutCoroutinesTest {

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var userRetrievalGrpcClient: UserRetrievalServiceGrpc.UserRetrievalServiceBlockingStub
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcServerConfigurer() : GrpcServerConfigurer {
            return GrpcServerConfigurer {
                t:ServerBuilder<*> -> t.executor(Executors.newFixedThreadPool(1))
            }
        }
    }

    @Test
    fun `test without coroutines`() {
        val task1 = RunnableTask(userRetrievalGrpcClient, "Client1", listOf(1L, 2L))
        val task2 = RunnableTask(userRetrievalGrpcClient, "Client2", listOf(3L, 4L))
        val task3 = RunnableTask(userRetrievalGrpcClient, "Client3", listOf(5L, 6L))

        val thread1 = Thread(task1)
        val thread2 = Thread(task2)
        val thread3 = Thread(task3)

        thread1.start()
        thread2.start()
        Thread.sleep(1_000)
        thread3.start()

        thread1.join()
        thread2.join()
        thread3.join()
        logger.info("${GREEN}All tasks completed, existing...$RESET")
        Thread.sleep(1_000)
    }

    class RunnableTask(private val client:UserRetrievalServiceGrpc.UserRetrievalServiceBlockingStub,
                       private val taskName: String, private val filterIdList: List<Long>) : Runnable {
        override fun run() {
            logger.info("$GREEN$taskName running to fetch users $filterIdList...$RESET")
            val start = System.currentTimeMillis()
            val request = GetUsersRequest.newBuilder().addAllUserIds(filterIdList).build()
            val users = client.getUsers(request)
            val end = System.currentTimeMillis()
            val time = (end - start) / 1000.0
            logger.info("$GREEN$taskName fetched users ${users.usersList.map { it.userId }} in $time seconds$RESET")
        }
    }
}

@GrpcService
@ConditionalOnProperty(name = ["withoutCoroutines"], matchIfMissing = false)
class UserRetrievalGrpcController(private val userService: UserService1) : UserRetrievalServiceGrpc.UserRetrievalServiceImplBase() {
    override fun getUsers(request: GetUsersRequest, responseObserver: StreamObserver<GetUsersResponse>) {
        logger.info("${BLUE}GrpcController received a request to fetch users ${request.userIdsList}$RESET")
        val users = userService.getUsers(request.userIdsList)
        val response = GetUsersResponse.newBuilder().addAllUsers(users).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}

@Service
class UserService1(private val userRepository: UserRepository1) {
    fun getUsers(filterIdList:List<Long>): List<User> {
        logger.info("${BLUE}Service fetching users $filterIdList$RESET")
        return userRepository.getUsers(filterIdList)
    }
}

@Repository
class UserRepository1 {
    private var allUsers:MutableList<User> = mutableListOf<User>()

    init {
        for (i in 1..10) {
            val user = User.newBuilder().setUserId(i.toLong()).setCountry(Thread.currentThread().name).build()
            allUsers.add(user)
        }
    }

    fun getUsers(filterIdList:List<Long>): List<User> {
        logger.info("${BLUE}Repository fetching users $filterIdList$RESET")
        if(filterIdList.contains(1) || filterIdList.contains(3)) {
            logger.info("${RED}Sleeping for 10 seconds while fetching users ${filterIdList}...$RESET")
            Thread.sleep(10_000)
        }
        return allUsers.filter { filterIdList.contains(it.userId) }
    }
}




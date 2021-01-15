package smartlims.testresultmgtsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;
import smartlims.testresultmgtsvc.service.impl.QueryServiceImpl;


@Configuration
@EnableAutoConfiguration(exclude = JdbcRepositoriesAutoConfiguration.class)
@ImportResource("/integration.xml")  
@SpringBootApplication
@Slf4j
public class TestresultmgtsvcApplication {
	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) throws Exception  {
		//SpringApplication.run(TestresultmgtsvcApplication.class, args);
		try {
			try {
				ctx = new SpringApplication(TestresultmgtsvcApplication.class).run(args);	
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}

			QueryServiceImpl.setJdbcTemplate((JdbcTemplate) getBean("jdbcTemplate"));
			// QueryServiceImpl.setNamedParameterJdbcTemplate((NamedParameterJdbcTemplate) getBean("namedParameterJdbcTemplate"));

			log.info("server is ready ...");

			System.out.println("Hit Enter to terminate");
			System.in.read();

		}
		finally {
			ctx.close();
		}		
	}

	public static Object getBean(final String name) {
		return ctx.getBean(name);
	}
}

package openmam.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import openmam.worker.commands.CallVantageWorkflow;
import openmam.worker.dto.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
class WorkerApplicationTests {

	@Value("${vantage.base.url}")
	private String vantageBaseUrl;
	@Value("${vantage.base.path}")
	private String vantageBasePath;

	@Test
	void contextLoads() {

	}

	@Test
	void testCallVantageWorkflow() {

		var taskPayload = new CallVantageWorkflow.VantageOpenMAMAdditionalJobsInput() {
			{
				vantageWorkflowID = "a6b85d9a-baf4-4cb3-8d49-e2d693ebab96";
				vantageWorkflowParameters = new CallVantageWorkflow.VantageWorkflowParameters() {
					{
						jobName = "Created from OpenMAM!";
						attachments = new ArrayList<>();
						labels = new ArrayList<>();
						medias = Arrays.asList(new CallVantageWorkflow.VantageWorkflowMedia[] {
								new CallVantageWorkflow.VantageWorkflowMedia() {
									{
										identifier = "1964e499-46f8-44a6-b4b9-d9c47e9a3899";
										files = Arrays.asList(vantageBasePath + "IN\\test.mp4");
										name = "MP4";
									}
								}
						});
						variables = Arrays.asList(new CallVantageWorkflow.VantageWorkflowVariable() {
							{
								identifier = "769914cd-8b71-48e7-91ec-0a9ea5747eb5";
								defaultValue = "";
								typeCode = "Uri";
								value = vantageBasePath + "OUT\\";
								name = "Folder_Path_Output";
							}
						}, new CallVantageWorkflow.VantageWorkflowVariable() {
							{
								identifier = "c0caf271-9cd2-4dd3-b703-36fd194ae8db";
								defaultValue = "1500";
								typeCode = "Int32";
								value = "2000";
								name = "Debit_Video";
							}
						}, new CallVantageWorkflow.VantageWorkflowVariable() {
							{
								identifier = "3842ebbc-b726-4034-b446-557a4df3212c";
								defaultValue = "";
								typeCode = "String";
								value = "FromOpenMAM";
								name = "File_Output_Name";
							}
						});
					}
				};
			}
		};

		var objectMapper = new ObjectMapper().setPropertyNamingStrategy(
				new PropertyNamingStrategies.UpperCamelCaseStrategy()
		);
		var jsonNode = objectMapper.convertValue(taskPayload, JsonNode.class);
		var task = new Task() {
			{
				additionalJobInputs = jsonNode;
			}
		};
		CallVantageWorkflow.handleTask(task);
	}
}

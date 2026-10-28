package com.agrinex;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The backend exposes exactly the operations of contracts/openapi.yaml: none missing and none invented (spec P5,
 * section 41). Path variable names are ignored; the request and response bodies are checked by the flow tests.
 */
class ContractCoverageIntegrationTest extends IntegrationTestSupport {

	static final Path CONTRACT = Path.of("..", "contracts", "openapi.yaml");

	static final Set<String> HTTP_METHODS = Set.of("get", "put", "post", "delete", "patch");

	/** The servlet error path is the container's error page, not an API operation. */
	static final String ERROR_PATH = "/error";

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	RequestMappingHandlerMapping handlerMapping;

	@Test
	void theImplementedOperationsAreExactlyTheContractOperations() throws IOException {
		Set<String> contract = contractOperations();
		assertThat(contract).hasSize(54);
		assertThat(implementedOperations()).isEqualTo(contract);
	}

	@SuppressWarnings("unchecked")
	private static Set<String> contractOperations() throws IOException {
		Map<String, Object> document;
		try (Reader reader = Files.newBufferedReader(CONTRACT)) {
			document = new Yaml().load(reader);
		}
		Set<String> operations = new TreeSet<>();
		((Map<String, Map<String, Object>>) document.get("paths")).forEach((path, item) -> item.keySet()
			.stream()
			.filter(HTTP_METHODS::contains)
			.forEach((method) -> operations.add(method.toUpperCase(Locale.ROOT) + " " + normalize(path))));
		return operations;
	}

	private Set<String> implementedOperations() {
		Set<String> operations = new TreeSet<>();
		for (RequestMappingInfo info : this.handlerMapping.getHandlerMethods().keySet()) {
			for (String path : info.getPathPatternsCondition().getPatternValues()) {
				if (!path.equals(ERROR_PATH)) {
					info.getMethodsCondition()
						.getMethods()
						.forEach((method) -> operations.add(method.name() + " " + normalize(path)));
				}
			}
		}
		return operations;
	}

	private static String normalize(String path) {
		return path.replaceAll("\\{[^}]+}", "{}");
	}

}

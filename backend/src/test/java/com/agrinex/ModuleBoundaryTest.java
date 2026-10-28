package com.agrinex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Module boundaries of the modular monolith (spec section 12, 27): a module uses another module only through the
 * public types at that module's root package (its API, ports and records), never through its web, service,
 * domain, repository or other internal packages, so no module reaches another module's tables. The shared
 * {@code common} package depends on no module.
 */
class ModuleBoundaryTest {

	static final Path SOURCES = Path.of("src", "main", "java", "com", "agrinex");

	static final Pattern AGRINEX_IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?com\\.agrinex\\.([a-z]+)((?:\\.[a-z][a-zA-Z0-9]*)*)\\.([A-Z][\\w.]*);",
			Pattern.MULTILINE);

	static final String COMMON = "common";

	@Test
	void modulesUseOnlyTheRootApiOfOtherModules() throws IOException {
		List<String> violations = new ArrayList<>();
		int crossModuleImports = 0;
		try (Stream<Path> files = Files.walk(SOURCES)) {
			for (Path file : files.filter((path) -> path.toString().endsWith(".java")).toList()) {
				Path relative = SOURCES.relativize(file);
				if (relative.getNameCount() < 2) {
					continue;
				}
				String module = relative.getName(0).toString();
				Matcher imports = AGRINEX_IMPORT.matcher(Files.readString(file));
				while (imports.find()) {
					String target = imports.group(1);
					boolean internal = !imports.group(2).isEmpty();
					boolean crossesModules = !target.equals(module) && !target.equals(COMMON);
					crossModuleImports += crossesModules ? 1 : 0;
					if ((crossesModules && internal) || (module.equals(COMMON) && !target.equals(COMMON))) {
						violations.add(relative + " -> " + imports.group().strip());
					}
				}
			}
		}
		assertThat(crossModuleImports).as("the scan must see the modules' API imports").isPositive();
		assertThat(violations).isEmpty();
	}

}

# Contributing to FIT4J

Thank you for your interest in contributing to FIT4J! This document provides guidelines and instructions for contributing.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

## How Can I Contribute?

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- Steps to reproduce the bug
- Expected behavior
- Actual behavior
- Environment details (Java version, Kotlin version, Spring Boot version, etc.)
- Any relevant logs or error messages

### Suggesting Enhancements

We welcome suggestions for improvements! Please create an issue with:
- A clear description of the enhancement
- Use cases or examples
- Potential implementation approach (if you have one)

### Pull Requests

1. **Fork the repository**
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**:
   - Follow the existing code style
   - Add tests for new functionality
   - Update documentation as needed
   - Ensure all tests pass locally
4. **Commit your changes**:
   ```bash
   git commit -m "Add: description of your changes"
   ```
   Use clear, descriptive commit messages.
5. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
6. **Open a Pull Request**:
   - Provide a clear description of the changes
   - Reference any related issues
   - Ensure CI checks pass

## Development Setup

### Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/harezmi/fit4j.git
   cd fit4j
   ```

### Prerequisites

- Java 17 or higher
- Gradle 7.x or higher
- Git

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Running Specific Tests

```bash
./gradlew test --tests "org.fit4j.*"
```

## Code Style

- Follow Kotlin coding conventions
- Use 4 spaces for indentation (not tabs)
- Follow existing code style patterns
- Use meaningful variable and function names
- Add KDoc comments for public APIs

## Testing Guidelines

- All new features should include tests
- Tests should be clear and focused
- Use descriptive test names (Kotlin backtick notation is welcome)
- Ensure tests are independent and can run in any order

## Documentation

- Update README.md if you add new features
- Add Javadoc/KDoc for public APIs
- Update examples if behavior changes
- Keep documentation clear and concise

## License

By contributing to FIT4J, you agree that your contributions will be licensed under the Apache License 2.0, the same license as the project.

## Questions?

If you have questions about contributing, please:
- Open an issue with the `question` label
- Check existing issues and discussions
- Review the documentation

Thank you for contributing to FIT4J! 🎉


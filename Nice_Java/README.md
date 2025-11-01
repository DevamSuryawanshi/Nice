# Nice_Java - Travel Optimizer

## Overview
Maven-based Java 8 project for optimized travel schedule generation with Hugging Face integration.

## Usage
```bash
mvn clean install
java -cp target/classes:target/lib/* com.nice.travel.Main --gen_trip_summary true --input routes.json
java -cp target/classes:target/lib/* com.nice.travel.Main --gen_trip_summary false --input routes.json
```

## Environment Variables
- `HUGGINGFACE_API_KEY`: Required for trip summary generation

## Testing
```bash
mvn test
```

## Features
- Dijkstra's algorithm for route optimization
- Time/Cost/Hops criteria with tie-breaking
- Hugging Face API integration for travel summaries
- Comprehensive test suite (7 test cases)
- Maven-compliant project structure
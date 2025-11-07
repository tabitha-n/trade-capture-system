# Reflection on Steps 1–4: Setup, Testing, and Feature Implementation

---

## Step 1: Local Environment Setup
The setup process went smoothly, including cloning the repository, configuring dependencies, and running both backend and frontend successfully. The only issue was forgetting to replace `YOUR_USERNAME` with my actual GitHub username, which then required adjusting the H2 database configuration in `application.properties`. I also noticed my Git pushes appeared on both my repository and the academy’s, so I resolved this by setting both the head and base repositories to my own during pull requests.

**Key takeaway:** always verify your Git and environment setup early to prevent cross-repository and configuration issues.

---

## Step 2–4: Testing and Feature Implementation

### What Worked Well
- Breaking larger tasks into smaller enhancements made the work more manageable.  
- Following business requirements closely ensured the code aligned with real trading use cases.  
- Using service-layer methods and DTOs reinforced separation of concerns in the Spring Boot application.  
- Testing API endpoints with Postman allowed quick verification of search, filter, and dashboard functionality.  
- Running tests in VS Code’s Test Explorer.  
- Documenting each test fix clarified the reasoning behind changes and will make future debugging faster and easier.  

### Challenges
- Designing tests to cover both success and failure scenarios required careful planning and understanding of business logic.  
- Some tests initially failed due to small mismatches or assumptions, such as expecting HTTP 200 instead of 201 Created.
- Debugging these failures involved closely reading stack traces and validation error messages.
- Working on cashflow calculations highlighted the importance of using the correct data types, maintaining decimal precision, and applying consistent rounding in financial computations.  

### Learning
- Learned to interpret failing test cases and exception messages to quickly identify root causes.  
- Understood the difference between test failures caused by code vs. by the test itself.
- Writing precise and meaningful assertions reinforced the importance of accuracy in tests.  
- Debugging null-pointer exceptions improved understanding of Java object references and service interactions.  
- Saw how small code changes could impact multiple tests, emphasizing the interconnectedness of the system.  
- Gained hands-on experience using VS Code tools such as Test Explorer. 
- Learned how documenting each test fix helps make future debugging clearer and faster.  
- Realized that even small errors in business logic can cascade into multiple failures, highlighting the need for thorough validation.  

### Next Steps / Improvements
- Complete RSQL query support for power users to allow complex searches.  
- Implement the daily summary endpoint and `DailySummaryDTO` for more comprehensive dashboard metrics.  
- Improve my 'Test & Debug' skills.

---
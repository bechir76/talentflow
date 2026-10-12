package com.talentflow.demo;

import java.util.List;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;

/**
 * Fictional demo content. All people, e-mail addresses and phone numbers are invented.
 */
final class DemoData {

    /** Fixed ids of the demo accounts in infra/keycloak/realm-export.json. */
    static final String CANDIDATE_USER = "8b1c6f0e-3d4a-4f6b-9a51-0c7d2e9f1a01";
    static final String RECRUITER_USER = "8b1c6f0e-3d4a-4f6b-9a51-0c7d2e9f1a02";
    static final String MANAGER_USER = "8b1c6f0e-3d4a-4f6b-9a51-0c7d2e9f1a03";
    static final String RECRUITER_NAME = "Anna Keller";
    static final String MANAGER_NAME = "Martin Richter";

    private DemoData() {
    }

    record JobSpec(String key, String title, String department, String location, WorkMode workMode, Integer salaryMin,
            Integer salaryMax, JobStatus status, int postedDaysAgo, List<String> skills, String description) {
    }

    record Person(String name, String email, String phone, String city, String headline, String summary,
            List<String> skills, List<String> experience) {
    }

    /**
     * One application: who, for which job, how many days ago, and the stages it went through (in order).
     */
    record ApplicationSpec(Person person, String jobKey, int appliedDaysAgo, List<ApplicationStage> path,
            String coverLetter, boolean linkedToDemoAccount) {
    }

    static final List<JobSpec> JOBS = List.of(
            new JobSpec("java", "Senior Java Developer", "Engineering", "Berlin, Germany", WorkMode.HYBRID, 75000, 95000,
                    JobStatus.PUBLISHED, 24, List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes"), """
                    About the role
                    We are looking for a Senior Java Developer to join our Engineering team in Berlin. You will design and build reliable backend services for our European platform.

                    What you will do
                    - Design scalable services with Java and Spring Boot
                    - Own features from design to production together with product and design
                    - Mentor engineers and improve our engineering practices

                    What you bring
                    At least five years of backend experience, solid SQL skills and a pragmatic, collaborative approach."""),
            new JobSpec("frontend", "Frontend Engineer (React)", "Engineering", "Remote, Germany", WorkMode.REMOTE, 60000, 80000,
                    JobStatus.PUBLISHED, 18, List.of("React", "TypeScript", "CSS", "Testing"), """
                    About the role
                    Build accessible, fast interfaces for recruiters and candidates across Europe.

                    What you will do
                    - Develop features in React and TypeScript
                    - Shape our design system together with product design
                    - Write tests that keep the product reliable

                    What you bring
                    Three or more years with React, a good eye for detail and care for accessibility."""),
            new JobSpec("design", "Product Designer", "Product & Design", "Munich, Germany", WorkMode.HYBRID, 58000, 72000,
                    JobStatus.PUBLISHED, 15, List.of("Figma", "User Research", "Prototyping", "Design Systems"), """
                    About the role
                    Turn complex hiring workflows into simple, inclusive experiences.

                    What you will do
                    - Run user research with recruiters and candidates
                    - Design flows, prototypes and components in Figma
                    - Evolve our design system

                    What you bring
                    A portfolio that shows research-driven product work and strong interaction design skills."""),
            new JobSpec("data", "Senior Data Analyst", "Data", "Cologne, Germany", WorkMode.REMOTE, 62000, 78000,
                    JobStatus.PUBLISHED, 12, List.of("SQL", "Python", "Tableau", "Statistics"), """
                    About the role
                    Help teams make better decisions with trustworthy data.

                    What you will do
                    - Build dashboards and analyses for product and sales
                    - Define metrics together with stakeholders
                    - Improve data quality and documentation

                    What you bring
                    Strong SQL, experience with Python and a clear way of communicating insights."""),
            new JobSpec("devops", "DevOps Engineer", "Engineering", "Frankfurt, Germany", WorkMode.HYBRID, 70000, 88000,
                    JobStatus.PUBLISHED, 9, List.of("Kubernetes", "Terraform", "AWS", "CI/CD"), """
                    About the role
                    Keep our platform secure, observable and easy to ship to.

                    What you will do
                    - Run our Kubernetes clusters and infrastructure as code
                    - Build CI/CD pipelines with the product teams
                    - Improve monitoring and incident response

                    What you bring
                    Hands-on experience with Kubernetes, Terraform and a public cloud."""),
            new JobSpec("sales", "Enterprise Account Executive", "Sales", "Hamburg, Germany", WorkMode.ONSITE, 65000, 85000,
                    JobStatus.PUBLISHED, 6, List.of("B2B Sales", "Salesforce", "Negotiation", "German"), """
                    About the role
                    Win and grow relationships with HR teams of large European companies.

                    What you will do
                    - Own the full sales cycle for enterprise customers
                    - Work closely with customer success and product
                    - Represent TalentFlow at events in the DACH region

                    What you bring
                    Proven B2B SaaS sales success and fluent German and English."""),
            new JobSpec("people", "People Operations Manager", "People", "Berlin, Germany", WorkMode.ONSITE, null, null,
                    JobStatus.DRAFT, 3, List.of("HR Operations", "Labour Law", "HRIS"), """
                    About the role
                    Shape the employee experience from onboarding to offboarding. This posting is still a draft."""),
            new JobSpec("success", "Customer Success Manager", "Customer Success", "Munich, Germany", WorkMode.HYBRID, 52000, 64000,
                    JobStatus.CLOSED, 60, List.of("Onboarding", "Account Management", "German"), """
                    About the role
                    Help our customers succeed with TalentFlow. This position has been filled."""));

    private static Person person(String name, String city, String headline, String summary, List<String> skills,
            List<String> experience) {
        String email = name.toLowerCase().replace(' ', '.').replace("ü", "ue").replace("ö", "oe").replace("ä", "ae")
                .replace("é", "e") + "@example.org";
        String phone = "+49 151 " + Math.abs(name.hashCode() % 9000000 + 1000000);
        return new Person(name, email, phone, city, headline, summary, skills, experience);
    }

    static final Person CLARA = new Person("Clara Candidate", "candidate@talentflow.dev", "+49 30 5550100", "Berlin",
            "Backend Developer", "Backend developer with 4 years of experience building Java services and REST APIs.",
            List.of("Java", "Spring Boot", "PostgreSQL", "Figma"),
            List.of("2022 - present  Backend Developer, Fintech GmbH, Berlin. Java and Spring Boot services, PostgreSQL.",
                    "2020 - 2022  Junior Developer, Webagentur Nord, Hamburg. Internal tools and APIs."));

    private static final Person LENA = person("Lena Schmidt", "Berlin", "Senior Backend Engineer",
            "Backend engineer with 8+ years of experience building scalable, resilient services for European technology companies.",
            List.of("Java", "Kotlin", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes", "AWS"),
            List.of("2021 - present  Senior Backend Engineer, Delivery Platform SE, Berlin. Led Java microservices handling millions of daily orders.",
                    "2018 - 2021  Backend Engineer, Fashion Commerce AG, Berlin. Spring Boot services, automated testing, faster deployments.",
                    "2016 - 2018  Software Engineer, Logistics Group, Hamburg. Java applications and PostgreSQL data pipelines."));
    private static final Person AMIRA = person("Amira Haddad", "Munich", "Java Software Engineer",
            "Java engineer with 6 years of experience in process mining and data-heavy backends.",
            List.of("Java", "Spring Boot", "Docker", "Kafka", "PostgreSQL"),
            List.of("2020 - present  Java Engineer, Process Analytics GmbH, Munich. Spring Boot services and Docker based deployments.",
                    "2018 - 2020  Developer, Insurance IT, Munich. Java backend for policy management."));
    private static final Person JONAS = person("Jonas Weber", "Leipzig", "Cloud Platform Engineer",
            "Platform engineer with 7 years of experience in Java and cloud infrastructure.",
            List.of("Java", "Spring Boot", "AWS", "Terraform", "Kubernetes", "Docker", "PostgreSQL"),
            List.of("2019 - present  Cloud Engineer, Mobility Platform, Munich. Kubernetes and AWS for Java services.",
                    "2017 - 2019  Java Developer, Travel Tech, Berlin."));
    private static final Person ELISA = person("Elisa Fischer", "Potsdam", "Senior Java Engineer",
            "Senior Java engineer with 9 years of experience, focused on clean architecture and mentoring.",
            List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes"),
            List.of("2018 - present  Senior Java Engineer, Payment Services, Berlin.", "2015 - 2018  Java Developer, Bank IT, Frankfurt."));
    private static final Person PAUL = person("Paul Meier", "Dresden", "Software Developer",
            "Developer with 2 years of experience in PHP web development, learning Java.",
            List.of("PHP", "JavaScript", "MySQL", "Java"),
            List.of("2023 - present  Web Developer, Agentur Ost, Dresden. PHP and JavaScript websites."));
    private static final Person DAVID = person("David Klein", "Berlin", "Java Developer",
            "Java developer with 5 years of experience in e-commerce backends.",
            List.of("Java", "Spring Boot", "PostgreSQL", "Docker"),
            List.of("2021 - present  Java Developer, Online Retail SE, Berlin.", "2019 - 2021  Junior Developer, Shop Software GmbH."));
    private static final Person SOPHIA = person("Sophia Müller", "Cologne", "Frontend Engineer",
            "Frontend engineer with 5 years of experience in React and TypeScript, passionate about accessibility.",
            List.of("React", "TypeScript", "CSS", "Testing", "Storybook"),
            List.of("2021 - present  Frontend Engineer, Neobank, Berlin. React, TypeScript and design systems.",
                    "2019 - 2021  Web Developer, Media House, Cologne."));
    private static final Person LUCAS = person("Lucas Bernard", "Remote", "UI Engineer",
            "UI engineer with 3 years of experience in Vue and React.",
            List.of("React", "Vue", "CSS", "JavaScript"),
            List.of("2022 - present  UI Engineer, SaaS Startup, Paris.", "2021 - 2022  Junior Frontend Developer, Agency, Lyon."));
    private static final Person MATTEO = person("Matteo Rossi", "Hamburg", "Frontend Developer",
            "Frontend developer with 4 years of experience in TypeScript and testing.",
            List.of("TypeScript", "React", "Testing", "CSS"),
            List.of("2020 - present  Frontend Developer, Payment Startup, Hamburg."));
    private static final Person NINA = person("Nina Kowalski", "Cologne", "Data Analyst",
            "Data analyst with 6 years of experience in SQL, Python and Tableau dashboards.",
            List.of("SQL", "Python", "Tableau", "Statistics", "dbt"),
            List.of("2019 - present  Data Analyst, Energy Group, Cologne. KPI dashboards in Tableau.",
                    "2018 - 2019  Junior Analyst, Market Research Institute."));
    private static final Person FELIX = person("Felix Wagner", "Bonn", "Business Analyst",
            "Business analyst with 3 years of experience in Excel modelling and SQL.",
            List.of("SQL", "Excel", "Power BI"),
            List.of("2022 - present  Business Analyst, Logistics Group, Bonn."));
    private static final Person HANNAH = person("Hannah Becker", "Frankfurt", "DevOps Engineer",
            "DevOps engineer with 5 years of experience in Kubernetes, Terraform and GitLab CI/CD on AWS.",
            List.of("Kubernetes", "Terraform", "AWS", "CI/CD", "Linux"),
            List.of("2020 - present  DevOps Engineer, Exchange Platform, Frankfurt.", "2019 - 2020  System Administrator, Hosting Provider."));
    private static final Person OMAR = person("Omar Aziz", "Mainz", "Site Reliability Engineer",
            "SRE with 4 years of experience in Kubernetes and observability.",
            List.of("Kubernetes", "Prometheus", "Go", "CI/CD"),
            List.of("2021 - present  SRE, Streaming Service, Berlin."));
    private static final Person JULIA = person("Julia Hoffmann", "Hamburg", "Account Executive",
            "B2B sales professional with 7 years of SaaS experience and fluent German.",
            List.of("B2B Sales", "Salesforce", "Negotiation", "German", "English"),
            List.of("2019 - present  Account Executive, HR Software AG, Hamburg.", "2017 - 2019  Sales Manager, Media Group."));
    private static final Person TIM = person("Tim Schulz", "Berlin", "Product Designer",
            "Product designer with 5 years of experience in Figma, user research and prototyping.",
            List.of("Figma", "User Research", "Prototyping", "Design Systems"),
            List.of("2020 - present  Product Designer, Mobility App, Berlin.", "2019 - 2020  UX Designer, Agency."));

    private static final List<ApplicationStage> TO_SCREENING = List.of(ApplicationStage.SCREENING);
    private static final List<ApplicationStage> TO_INTERVIEW = List.of(ApplicationStage.SCREENING, ApplicationStage.INTERVIEW);
    private static final List<ApplicationStage> TO_OFFER = List.of(ApplicationStage.SCREENING, ApplicationStage.INTERVIEW, ApplicationStage.OFFER);
    private static final List<ApplicationStage> TO_HIRED = List.of(ApplicationStage.SCREENING, ApplicationStage.INTERVIEW, ApplicationStage.OFFER, ApplicationStage.HIRED);

    static final List<ApplicationSpec> APPLICATIONS = List.of(
            new ApplicationSpec(LENA, "java", 9, TO_INTERVIEW, "I would love to bring my experience with high-volume Java services to your team.", false),
            new ApplicationSpec(AMIRA, "java", 7, TO_SCREENING, null, false),
            new ApplicationSpec(JONAS, "java", 14, TO_OFFER, null, false),
            new ApplicationSpec(ELISA, "java", 22, TO_HIRED, null, false),
            new ApplicationSpec(PAUL, "java", 12, List.of(ApplicationStage.REJECTED), null, false),
            new ApplicationSpec(DAVID, "java", 2, List.of(), null, false),
            new ApplicationSpec(CLARA, "java", 5, TO_SCREENING, "Your platform sounds exciting and I would like to grow into a senior role.", true),
            new ApplicationSpec(CLARA, "design", 1, List.of(), null, true),
            new ApplicationSpec(SOPHIA, "frontend", 8, TO_INTERVIEW, null, false),
            new ApplicationSpec(LUCAS, "frontend", 4, List.of(), null, false),
            new ApplicationSpec(MATTEO, "frontend", 6, TO_SCREENING, null, false),
            new ApplicationSpec(NINA, "data", 10, TO_OFFER, null, false),
            new ApplicationSpec(FELIX, "data", 7, List.of(ApplicationStage.SCREENING, ApplicationStage.REJECTED), null, false),
            new ApplicationSpec(HANNAH, "devops", 6, TO_INTERVIEW, null, false),
            new ApplicationSpec(OMAR, "devops", 3, List.of(), null, false),
            new ApplicationSpec(JULIA, "sales", 4, TO_SCREENING, null, false),
            new ApplicationSpec(TIM, "design", 9, TO_INTERVIEW, null, false));
}

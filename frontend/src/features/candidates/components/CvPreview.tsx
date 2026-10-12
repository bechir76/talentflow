import Icon from "@/components/common/Icon";

export default function CvPreview() {
  return <article className="cv-panel panel">
    <div className="cv-toolbar">
      <div><span className="pdf-icon">PDF</span><div><strong>Lena_Schmidt_CV.pdf</strong><small>Updated 2 days ago · 284 KB</small></div></div>
      <div><button className="icon-button" aria-label="Download CV"><Icon name="download"/></button><span>1 / 2</span><button className="zoom-button">−</button><b>85%</b><button className="zoom-button">+</button></div>
    </div>
    <div className="pdf-canvas">
      <div className="cv-page">
        <div className="cv-document-head">
          <div><h2>LENA<br/>SCHMIDT</h2><span>SENIOR BACKEND ENGINEER</span></div>
          <div><p>lena.schmidt@email.de</p><p>Berlin, Germany</p><p>+49 151 234 5678</p></div>
        </div>
        <div className="cv-document-body">
          <aside>
            <section><h4>PROFILE</h4><p>Backend engineer with 8+ years of experience building scalable, resilient services for European technology companies.</p></section>
            <section><h4>SKILLS</h4><strong>Languages</strong><p>Java, Kotlin, SQL</p><strong>Frameworks</strong><p>Spring Boot, Hibernate</p><strong>Infrastructure</strong><p>Docker, AWS, Terraform</p></section>
            <section><h4>EDUCATION</h4><strong>M.Sc. Computer Science</strong><p>Technische Universität Berlin<br/>2014–2016</p></section>
            <section><h4>LANGUAGES</h4><p>German — Native<br/>English — Fluent</p></section>
          </aside>
          <main>
            <section><h4>EXPERIENCE</h4>
              <div className="cv-job"><b>2021 — PRESENT</b><h3>Senior Backend Engineer</h3><span>Delivery Hero · Berlin</span><p>Led the design of high-volume Java microservices processing over 2M daily transactions. Improved system reliability to 99.98% and mentored five engineers.</p></div>
              <div className="cv-job"><b>2018 — 2021</b><h3>Backend Engineer</h3><span>Zalando · Berlin</span><p>Developed Spring Boot services for the European commerce platform. Introduced automated testing practices and reduced deployment time by 40%.</p></div>
              <div className="cv-job"><b>2016 — 2018</b><h3>Software Engineer</h3><span>Otto Group · Hamburg</span><p>Built and maintained Java applications and PostgreSQL data pipelines for the logistics division.</p></div>
            </section>
            <section><h4>SELECTED PROJECT</h4><h3>Order Processing Platform</h3><p>Technical lead for a distributed event-driven system using Java, Spring Boot, PostgreSQL, Docker and Kafka.</p></section>
          </main>
        </div>
      </div>
    </div>
  </article>;
}

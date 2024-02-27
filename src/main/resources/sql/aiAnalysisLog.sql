CREATE TABLE ai_analysis_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image_path VARCHAR(255),
    success BOOLEAN,
    message VARCHAR(255),
    class_id INT,
    confidence DECIMAL(5,4),
    request_timestamp TIMESTAMP UNSIGNED,
    response_timestamp TIMESTAMP UNSIGNED
);

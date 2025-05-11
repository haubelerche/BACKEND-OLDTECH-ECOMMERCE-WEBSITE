-- Drop existing constraint if it exists
SET FOREIGN_KEY_CHECKS=0;

-- Alter table columns to ensure types match
ALTER TABLE complaint MODIFY COLUMN complainant_id INT UNSIGNED;
ALTER TABLE complaint MODIFY COLUMN respondent_id INT UNSIGNED;
ALTER TABLE complaint MODIFY COLUMN order_id INT UNSIGNED;

-- Add the foreign key with matching column types
ALTER TABLE complaint 
ADD CONSTRAINT FK_complainant_user 
FOREIGN KEY (complainant_id) 
REFERENCES user(user_id);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS=1; 
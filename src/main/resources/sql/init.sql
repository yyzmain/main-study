
use `main`;
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`
(
    `student_id`   int(10) ,
    `student_name` varchar(50)    NOT NULL,
    `gender`       int(1)       NOT NULL,
    `age`          int(3)           NOT NULL,
    PRIMARY KEY (`student_id`)
) ;
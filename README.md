<img src="/bug.jpg" alt="" width="200px">

Front-end part of Bug Tracker web application: <a href="https://github.com/MichalKrol2020/angular-bug-tracker">Angular Bug Tracker</a>

# Bug Tracker
The Bug Tracker is a web application built with the Spring Boot framework and Angular CLI, that allows software development teams to track and manage bugs throughout the development process. With this tool, teams can easily report, assign, and track bugs, as well as collaborate on solutions and track progress over time. The Bug Tracker is easy to use and customizable, making it a valuable tool for any software development team.

## How does it work?

The application uses JSON Web Tokens (JWT) for authorization, and users are assigned roles and authorities to control their access to different features.
 
Users can register, login, and reset passwords using the application. Once logged in, users can report bugs, view existing bugs, and collaborate with other team members to find solutions. The application also allows users to create and manage projects (Project Leaders feature), and view a dashboard that provides an overview of the status of all bugs and projects.

To ensure security, the application uses pre-authorization for certain methods, forbidding certain actions based on a user's role and permissions. All data is stored in a database, including information about bugs, projects, and users.

## Features

* User registration, login, and password reset functionality
* User roles and authorities to control access to features
* JSON Web Tokens (JWT) for secure authorization
* Bug reporting, viewing, and management
* Project creation and management
* Pre-authorization for certain methods to ensure security
* Data storage for bugs, projects, and users in a database
* Dashboard with an overview of bug and project status
* Customizable and easy to use for software development teams
* Generating progress reports to track the development team's progress and identify areas for improvement



# Functionality

## Main cockpit

The cockpit of the Bug Tracker provides a high-level overview of bug and project status, as well as key performance metrics.

The main tile displays performance numbers, including the total number of reported bugs, the number of bugs reported in the current month, and the number of fixed bugs. This information helps development teams stay on top of bug reports and prioritize their efforts to ensure that the most critical issues are addressed first.

The dashboard also includes a button to generate a complete progress report, which provides a detailed overview of bug and project status, as well as team performance metrics. 

The cockpit is designed to be easy to use and customizable, providing a comprehensive view of bug and project status to help development teams make informed decisions and track their progress over time.

![1](https://user-images.githubusercontent.com/106864921/227048625-f03a84f4-7339-4e99-bdea-67569c9a5748.jpg)

![2](https://user-images.githubusercontent.com/106864921/227176600-67fe125e-9a17-4bf1-afea-717dc4527069.jpg)


## Sidenav

The sidenav of the Bug Tracker provides easy access to all of the application's main features. The sidenav is responsive and adjusts its size according to the size of the current window, ensuring that users can access all features regardless of their device or screen size.

The sidenav contains links to the following features:

* Cockpit: Provides a high-level overview of bug and project status, as well as key performance metrics.
* Projects: Allows project leaders to create and manage projects, view project details, and track progress over time.
* Issues: Provides an overview of all reported bugs, as well as detailed information about each bug and its current status.
* Add: Allows users to quickly report new bugs and provide all relevant details.
* Notifications: Displays a list of all notifications related to bug reports, project updates, and other relevant events.
* Users: Provides a list of all users from the company who can be contacted through the application's built-in chat feature.

The sidenav is easy to use and ensures that users can access all of the application's main features with just a few clicks.

![2](https://user-images.githubusercontent.com/106864921/227050220-0fa22c25-6d6b-4814-88ec-f202ae757e1a.jpg)



## Projects

The Projects tab of the Bug Tracker provides an overview of all active projects, along with detailed information about bugs and participants.

The main table displays a list of all active projects, including the project name and description. Users can click on a project to view additional details, including a list of all bugs associated with the project and a list of all participants.

Project Leaders have access to additional functionality, including the ability to create new projects, assign and unassign participants, and manage bugs. They can also choose whether to display the bugs table or participants table by default, ensuring that they can easily access the information most relevant to their needs.

Users who are not Project Leaders have more limited access, and can only view project names and descriptions, as well as the list of bugs associated with each project. This ensures that all users can stay up-to-date on the status of active projects, while also maintaining the necessary levels of security and access control.

Overall, the Projects tab provides a comprehensive view of all active projects, along with detailed information about bugs and participants, to help development teams stay organized and work more efficiently.

<br>

* **Project Leader view:**

![3](https://user-images.githubusercontent.com/106864921/227051454-ba09910d-bff9-448f-93d8-8e16e13070d4.jpg)

![4](https://user-images.githubusercontent.com/106864921/227051785-f3179e87-1364-42ce-afb8-09ee2791acb8.jpg)

![5](https://user-images.githubusercontent.com/106864921/227052002-20d95346-1964-4f9e-aa86-aef9f22632bd.jpg)

![6](https://user-images.githubusercontent.com/106864921/227053421-4a4c69ed-ec39-4a20-8a7b-2742ed519e96.jpg)

![7](https://user-images.githubusercontent.com/106864921/227053741-647a93ea-4d7a-4b6f-a984-435e62e83a84.jpg)

<br>

* **User view:**

![8](https://user-images.githubusercontent.com/106864921/227054495-4756246b-c359-41f1-bd67-f56725c39bd8.jpg)


## Issues

The Issues tab of the Bug Tracker provides an overview of all reported bugs, along with detailed information about their classification, status, severity, and assigned user.

The main table displays a list of all reported bugs, including the bug name, description, classification, status, severity, project, assigned user, and creation date. Users can click on a bug to view additional details and make edits or updates as necessary.

Project Leaders have access to additional functionality, including the ability to edit all bugs, assign, unassign and reassign users to work on bugs, and delete bugs. This ensures that project leaders can manage bugs effectively and ensure that they are resolved in a timely manner.

Users who are not Project Leaders have more limited access, and can only edit and delete bugs that they have created and whose status is still NEW. Once a bug's status changes (e.g. due to being assigned to a user), only project leaders can edit it.

This access control ensures that users can view and edit bugs that are relevant to their work, while maintaining the necessary levels of security and access control. Overall, the Issues tab provides a comprehensive view of all reported bugs, along with the ability to manage and resolve them efficiently.

<br>

* **Project Leader view:**

![9](https://user-images.githubusercontent.com/106864921/227055499-fcce4107-c7cc-4e4e-a1f6-d27e8f3d85a4.jpg)

![10](https://user-images.githubusercontent.com/106864921/227175501-ff03d0e0-fb84-423b-a196-5df55ea67600.jpg)

<br>

* **User view:**

![10](https://user-images.githubusercontent.com/106864921/227056927-1ece110d-7f60-426f-8bf4-fbbed7c4c312.jpg)


## Add

The Add tab of the Spring Boot Bug Tracker provides a simple form for submitting newly discovered bugs. Users can choose the classification, severity, and project in which the bug occurred, along with a description of the issue.

Project Leaders have access to additional functionality, including the ability to assign a user to work on the bug during its creation. This ensures that new bugs are immediately assigned to the appropriate team member and can be resolved as quickly as possible.

Overall, the Add tab provides a simple and streamlined way to report bugs and ensure that they are addressed promptly and efficiently.

*Note: In future releases, in the right side of the Add tab, the bug creation and classification instructions will be provided as simple guide to help users properly report and classify bugs, ensuring that all issues are resolved as quickly and efficiently as possible.*

![11](https://user-images.githubusercontent.com/106864921/227057782-47ccc1a9-5802-4142-8c7d-2ad9abf98438.jpg)

![12](https://user-images.githubusercontent.com/106864921/227058293-428bd0d2-afa2-4958-bc66-b420e65a9fe6.jpg)


## Notifications

The Notifications tab of the Bug Tracker provides an easy way for users to stay informed about important events in the application. Notifications are sent automatically during key events, such as when a bug is reported (with notifications sent to the project leader), or when a project is deleted (with notifications sent to participants who will be unassigned).

In addition, users receive notifications when their bugs are edited or deleted by project leaders, ensuring that everyone stays up-to-date on any changes to their assigned tasks.

Overall, the Notifications tab helps ensure that all users are informed and engaged, and that everyone is working together effectively to resolve bugs and improve the application.

![13](https://user-images.githubusercontent.com/106864921/227060518-15a725a4-75aa-498a-a6ca-5b36ba42ce07.jpg)


## Chat

The Bug Tracker also includes a chat functionality, powered by the TalkJS API. This allows users to communicate with each other in real-time, discussing bugs and collaborating to resolve issues as quickly and efficiently as possible.

With the TalkJS API, users can easily send and receive messages, create chat rooms for specific projects or groups of users, and access advanced features such as file sharing and search. This makes it easy for everyone to stay connected and engaged, even when working on complex or long-term projects.

Overall, the chat functionality in the Bug Tracker provides a powerful and flexible way for users to collaborate and communicate effectively, helping to ensure that bugs are resolved as quickly and efficiently as possible.

![14](https://user-images.githubusercontent.com/106864921/227061613-e5372c31-292a-44f6-96d4-3ac5ae612444.jpg)

## Upcoming Releases

The upcoming Admin Dashboard in the Bug Tracker Web Application will provide powerful new tools for managing users, projects, and other key aspects of the application. Designed specifically for administrators, the dashboard will offer an intuitive and easy-to-use interface that makes it simple to manage all aspects of the application.

With the Admin Dashboard, administrators will be able to perform a range of key tasks, such as creating and managing user accounts, assigning roles and authorities, and approving new user registration requests. They'll also be able to view detailed information about all projects, bugs, and other key elements of the application, and easily make changes as needed.

Overall, the Admin Dashboard will be an essential tool for managing and maintaining the Spring Boot Bug Tracker, and will help ensure that the application runs smoothly and efficiently over the long term. Whether you're a project leader, a developer, or an administrator, the Admin Dashboard will provide the tools you need to get the job done right.

<br>
<br>
<br>

## Credits

Computer trouble shooting concept illustration
<a href="https://www.freepik.com/free-vector/computer-trouble-shooting-concept-illustration_18771510.htm#query=broken%20computer&position=1&from_view=keyword&track=ais">Image by storyset</a> on Freepik

Free vector push notifications concept illustration
<a href="https://www.freepik.com/free-vector/push-notifications-concept-illustration_12219838.htm#query=phone%20notification&position=3&from_view=keyword&track=ais">Image by storyset</a> on Freepik

Bug Tracker logo
<a href="https://www.shutterstock.com/image-vector/beetle-circuit-animal-insect-modern-logo-2029146809">Image by freestore 839</a> on shutterstock.com

Temporary profile images powered by
<a href="https://robohash.org/">Robohash</a>

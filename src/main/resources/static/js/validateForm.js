function validateForm() {
    const firstname = document.getElementById("firstname").value;
    const lastname = document.getElementById("lastname").value;
    const password = document.getElementById("password").value;
    const email = document.getElementById("email").value;
    //const admin = document.getElementById("admin").checked;

    const namePattern = /^[A-Z]+$/;
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,15}$/;
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!namePattern.test(firstname)) {
        alert("First name must contain only uppercase letters.");
        return false;
    }

    if (!namePattern.test(lastname)) {
        alert("Last name must contain only uppercase letters.");
        return false;
    }

    if (!passwordPattern.test(password)) {
        alert("Password must be between 8 and 15 characters long, and contain at least one uppercase letter, one lowercase letter, and one digit.");
        return false;
    }

    if (!emailPattern.test(email)) {
        alert("Email must be in a valid format.");
        return false;
    }

    return true;
}
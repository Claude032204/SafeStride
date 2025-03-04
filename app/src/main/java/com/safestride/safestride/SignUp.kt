package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import android.app.AlertDialog

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth  // Firebase Auth instance
    private lateinit var db: FirebaseFirestore  // Firestore instance

    private val RC_SIGN_IN = 9001  // Request code for Google Sign-In
    private val staticVerificationCode = "123456" // Static verification code for simulation
    private var emailFromGoogle: String? = null // Store the email after Google sign-in
    private var userIsVerified = false // Flag to check if user is verified with the code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // References to the fields in your layout
        val usernameEditText: EditText = findViewById(R.id.usernameField) // Username field
        val emailEditText: EditText = findViewById(R.id.emailField) // Email field
        val passwordEditText: EditText = findViewById(R.id.passwordField) // Password field
        val confirmPasswordEditText: EditText =
            findViewById(R.id.confirmPasswordField) // Confirm password field
        val signUpButton: Button = findViewById(R.id.signupButton)
        val alreadyHaveAccountText: TextView = findViewById(R.id.loginText)
        val eyeIconPassword: ImageView = findViewById(R.id.eyeIconPassword)
        val eyeIconConfirmPassword: ImageView = findViewById(R.id.eyeIconConfirmPassword)
        val passwordRequirementsText: TextView = findViewById(R.id.passwordRequirementsText)
        val googleSignUpButton: LinearLayout =
            findViewById(R.id.googleLogin) // Google Sign-Up Button

        // Navigate to Log In when "Already have an account?" is clicked
        alreadyHaveAccountText.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish()
        }


    // Check if the email field has been passed from the login screen (Google Sign-In)
        val email = intent.getStringExtra("email")
        email?.let {
            emailEditText.setText(it)  // Pre-fill the email field with the selected Google account's email
        }

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Web Client ID
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-Up Button Click Listener
        googleSignUpButton.setOnClickListener {
            // Sign out the previous Google user to allow account selection
            googleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    // After signing out, initiate the sign-in process again
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
        }

        // TextWatcher to monitor form fields
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = usernameEditText.text.isNotEmpty() &&
                        emailEditText.text.isNotEmpty() &&
                        passwordEditText.text.isNotEmpty() &&
                        confirmPasswordEditText.text.isNotEmpty() &&
                        isValidEmail(emailEditText.text.toString()) &&
                        passwordEditText.text.toString() == confirmPasswordEditText.text.toString() // Ensure passwords match
                signUpButton.isEnabled = isFormFilled
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // Attach TextWatcher to each field
        usernameEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Show password requirements only if password is not empty
                if (passwordEditText.text.isNotEmpty() && !isPasswordValid(passwordEditText.text.toString())) {
                    passwordRequirementsText.visibility = TextView.VISIBLE
                } else {
                    passwordRequirementsText.visibility = TextView.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        confirmPasswordEditText.addTextChangedListener(textWatcher)

        // Ensure password input is hidden initially
        passwordEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPasswordEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Eye Icon for password toggle
        eyeIconPassword.setOnClickListener {
            togglePasswordVisibility(passwordEditText, eyeIconPassword)
        }

        // Eye Icon for confirm password toggle
        eyeIconConfirmPassword.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, eyeIconConfirmPassword)
        }

        // Sign-Up Button Click Listener (Email/Password sign up)
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (!userIsVerified) {
                Toast.makeText(this, "Please verify your email first", Toast.LENGTH_SHORT).show()
            } else if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with Firebase sign-up logic
                registerUserInFirestore(username, emailFromGoogle!!, password)
            }
        }
    }

    // Handle Google Sign-In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Authenticate with Firebase after Google sign-in
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase authentication with Google credentials
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // Ensure the user is properly authenticated
                        if (user != null) {
                            emailFromGoogle = account.email
                            // Automatically populate email field
                            findViewById<EditText>(R.id.emailField).setText(account.email)
                            showVerificationDialog(account.email ?: "")
                        } else {
                            Toast.makeText(this, "User not authenticated. Please sign in again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Function to show the verification dialog
    private fun showVerificationDialog(email: String) {
        // Static code simulation
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_verification, null)

        val verificationCodeEditText = dialogView.findViewById<EditText>(R.id.verificationCodeEditText)
        val verifyButton = dialogView.findViewById<Button>(R.id.verifyButton)

        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()

        // Set the "Verify" button logic
        verifyButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString()
            if (enteredCode == staticVerificationCode) {  // Check if the entered code matches the static code
                userIsVerified = true
                dialog.dismiss() // Close the dialog
                Toast.makeText(this, "Verification successful! Please fill in your details.", Toast.LENGTH_SHORT).show()
                showRegistrationFields()  // Show registration fields
            } else {
                Toast.makeText(this, "Invalid code, please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // Function to show the registration fields after verification
    private fun showRegistrationFields() {
        val usernameEditText: EditText = findViewById(R.id.usernameField)
        val passwordEditText: EditText = findViewById(R.id.passwordField)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordField)

        // Enable the fields for username, password, and confirm password
        usernameEditText.isEnabled = true
        passwordEditText.isEnabled = true
        confirmPasswordEditText.isEnabled = true
    }

    // Register the user in Firestore after successful verification
    private fun registerUserInFirestore(username: String, email: String, password: String) {
        val user = hashMapOf(
            "username" to username,
            "email" to email
        )

        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    finish() // Close the sign-up activity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(
                this,
                "User not authenticated. Please sign in again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Basic email validation method
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation method
    private fun isPasswordValid(password: String): Boolean {
        val regex =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,}\$"
        return password.matches(regex.toRegex())
    }

    // Toggle password visibility
    private fun togglePasswordVisibility(passwordEditText: EditText, eyeIcon: ImageView) {
        if (passwordEditText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            eyeIcon.setImageResource(R.drawable.openeye)
        } else {
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eyeIcon.setImageResource(R.drawable.eyeclosed)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }
}

function renderFoot(){
  const footer = document.getElementById("footer");
  if(!footer) return;

  footer.innerHTML = `
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="logo-tittle">Hospital CMS</span>
          <p>&copy; ${new Date().getFullYear()} Hospital CMS. Allrights reserved.</p>
        </div>
        
        <div class="footer-columns">
          <div class="footer-column">
            <h4>Company</h4>
            <ul>
              <li><a href="#">About</a></li>
              <li><a href="#">Careers</a></li>
              <li><a href="#">Press</a></li>
            </ul?
          </div>
          
          <div class="footer-column">
            <h4>Support</h4>
            <ul>
              <li><a href="#">Account</a></li>
              <li><a href="#">Help Center</a></li>
              <li><a href="#">Contact</a></li>
            </ul>
          </div>
          
          <div class="footer-column">
            <h4>Legal</h4>
            <ul>
              <li><a href="#">Terms</a></li>
              <li><a href="#">Privacy Policy</a></li>
              <li><a href="#">Licensing</a></li>
            </ul>
          </div>
        </div>
      </div>
    </footer>`
}

renderFoot();
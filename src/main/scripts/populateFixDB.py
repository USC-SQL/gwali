#!/usr/bin/python
# -*- coding: utf-8 -*-

import sqlite3 as lite
import sys
def populateDB():
	try:
	    print "Populating fixes DB"
	    con = lite.connect('Fixes.db')
		
	    cur = con.cursor()  

	    cur.executescript("""
		DROP TABLE IF EXISTS Fixes;
		CREATE TABLE Fixes(Page TEXT, XPath TEXT, Pattern TEXT, Replacement TEXT);

		INSERT INTO Fixes VALUES('TestCaseDetection/simplepages/sample.html','html/body/input[1]/@placeholder','for text ranges at non-IE browsers','a');

		INSERT INTO Fixes VALUES('TestCaseDetection/simplepages/sample2.html','html/body/ul/descendant::text()','culo','.');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150611161902/index.html','html/body/footer/div/div/div[3]/div/ul[1]/descendant::text()','Pequeñas&nbsp;empresas&nbsp;','P');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150611161902/index.html','html/body/footer/div/div/div[3]/div/nav/ul[2]/descendant::text()','Universidades','Univ');
		
		INSERT INTO Fixes VALUES('ScrapBook/data/20150611171720/index.html','html/body/div[5]/div[1]/div/div/div[1]/p[2]/descendant::text()','Сообщество\s*Google Планета Земля','Сообщество');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150611171720/index.html','html/body/div[5]/div[1]/div/div/div[2]/p[2]/descendant::text()','Некоммерческих организаций','H');
		
		INSERT INTO Fixes VALUES('ScrapBook/data/20150612142537/index.html','html/body/form/div[5]/div/header/ul/li[6]/p[2]/descendant::text()','Iniciar sesión<\/a>', 'S</a>');
		
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614200743/index.html','html/body/div[8]/div/div[2]','Warum über rentalcars\.com buchen\?','über rentalcars.com buchen?');
		
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614202153/index.html','html/body/div[3]/div[3]/div[5]/div[1]/div[1]/ul/descendant::text()','aggiungi il tuo hotel','aggiungi');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614202153/index.html','html/body/div[3]/div[3]/div[1]/div/div[2]/div[1]/div[3]/div[2]/div[1]/form/div[3]/div/div[1]/div[1]/div/div[1]/input/@value','input value="inserisci una città o un aeroporto" autocomplete="off" tabindex="4"','input value="inserisci" autocomplete="off" tabindex="4"');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614202153/index.html','html/body/div[3]/div[3]/div[1]/div/div[2]/div[1]/div[3]/div[2]/div[1]/form/div[3]/div/div[1]/div[1]/div/div[2]/input/@value','input value="inserisci una città o un aeroporto" autocomplete="off" tabindex="5"','input value="inserisci" autocomplete="off" tabindex="5"');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150616171216/index.html','html/body/form/div[3]/div[2]/div[1]/div/div[2]/ul/descendant::text()','Решения для промышленности','Ре');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/footer/div/div/section/div/div[3]/div/descendant::text()','MyPlay Direct © 2015','M');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[1]/div[4]/div/div[1]/text()','http://www.myplaydirect.com/kiss" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.myplaydirect.com/kiss" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[2]/div[4]/div/div[1]/text()','http://ghostbustersstore.com/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://ghostbustersstore.com/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[3]/div[4]/div/div[1]/text()','http://www.popmarket.com/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.popmarket.com/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[4]/div[4]/div/div[1]/text()','http://friendstvshop.com/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://friendstvshop.com/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[5]/div[4]/div/div[1]/text()','http://www.breakingbadstore.com/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.breakingbadstore.com/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[6]/div[4]/div/div[1]/text()','http://www.myplaydirect.com/whitney-houston/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.myplaydirect.com/whitney-houston/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[7]/div[4]/div/div[1]/text()','http://shop.legendary.com/" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://shop.legendary.com/" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[8]/div[4]/div/div[1]/text()','http://www.michaeljackson.com/us/store" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.michaeljackson.com/us/store" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170806/index.html','html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[9]/div[4]/div/div[1]/text()','http://www.asapmobshop.com/home/all" target="_top">Tienda de Official</a></h3></div></span>  </div>\s+<div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para recibir alertas de la tienda','http://www.asapmobshop.com/home/all" target="_top">Tienda de Official</a></h3></div></span>  </div> <div class="views-field views-field-field-newsletter-list-id">        <div class="field-content"><div class="brand-detail-row newsletter-button">Regístrese para');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150616170037/index.html','html/body/div[2]/div[2]/div[1]/div[4]/div/div/div/form/div[1]/div[3]/div[2]/p[2]/input/@value','Geben Sie Ihren Vor- und Nachnamen an \(optional\)','Geben Sie ');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203006/index.html','html/body/form/div[18]/div[2]/div/div[4]/div/div/div/fieldset[4]/div[1]/span[3]/text()','11 Yaş üzeri','11 Yaş');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203006/index.html','html/body/form/div[18]/div[2]/div/descendant::text()','Rezervasyon Yapın','Rezervasyon');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/ul/li[3]/a/font/font/text()','class="hyperlink_link hyperlink_link_1177022" alt="Auszeichnungen" href="#" data-href="awards"><font><font>Auszeichnungen','class="hyperlink_link hyperlink_link_1177022" alt="Auszeichnungen" href="#" data-href="awards"><font><font>Auszeic');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/ul/li[9]/a/font/font/text()','class="hyperlink_link hyperlink_link_1177134" alt="Medienzentrum" href="#" data-href="media-centre"><font><font>Medienzentrum','class="hyperlink_link hyperlink_link_1177134" alt="Medienzentrum" href="#" data-href="media-centre"><font><font>Med');		
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/div[4]/div/div[8]/ul/li[1]/p[3]/descendant::text()','tritt in die Fußstapfen des letztjährigen Siegers Azurmendi in Larrabetzu','tritt');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/div[4]/div/div[8]/ul/li[4]/p/descendant::text()','dass die Jahre verbrachte er leitet die Küche an lokales Wahrzeichen Tetsuya die mehr','dass');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/div[6]/ul/li[3]/a/font/font/text()','class="hyperlink_link hyperlink_link_1449052" alt="Auszeichnungen" href="#" data-href="awards"><font><font>Auszeichnungen','class="hyperlink_link hyperlink_link_1449052" alt="Auszeichnungen" href="#" data-href="awards"><font><font>Auszeic');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614203943/index.html','html/body/div[1]/div[6]/ul/li[9]/a/font/font/text()','class="hyperlink_link hyperlink_link_1449134" alt="Medienzentrum" href="#" data-href="media-centre"><font><font>Medienzentrum','class="hyperlink_link hyperlink_link_1449134" alt="Medienzentrum" href="#" data-href="media-centre"><font><font>Med');		


		INSERT INTO Fixes VALUES('ScrapBook/data/20150614204452/index.html','html/body/div[2]/div/footer/div/section[2]/div/form/span[2]/font/font/input','INSCRIVEZ-VOUS MAINTENANT','INSCRIVEZ');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614204452/index.html','html/body/div[2]/div/div[3]/nav/ul/descendant::text()','Notre recherche|Apprendre et Enseigner','it');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614204452/index.html','html/body/div[2]/div/div[3]/div[6]/div[2]/article[1]/section/a','Commencez à explorer','Commencez');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614204452/index.html','html/body/div[2]/div/div[3]/div[6]/div[2]/article[4]/section[1]/h1/descendant::text()','de cadeaux uniques','de');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150614204452/index.html','html/body/div[2]/div/div[3]/div[6]/div[2]/article[4]/section[2]/h1/descendant::text()','avec les experts','avec');


		INSERT INTO Fixes VALUES('ScrapBook/data/20150615225355/index.html','html/body/div[7]/div[1]/div[1]/ul/descendant::text()','financiamiento</a>','fin</a>');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150616145500/index.html','html/body/div[1]/div[3]/footer/div/p[1]/span[5]/descendant::text()','Privacidad y Políticas|Acerca de nuestros Anuncios','it');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150610172432/index.html','html/body/div[1]/section[1]/div/form/span/input/@placeholder','Rechercher dans','R');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150610165346/index.html','html/body/div[2]/div[2]/div/div[3]/div[3]/h3/text()','Contactar um conselheiro','Contactar');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150610165346/index.html','html/body/div[2]/div[1]/div[2]/div/div[8]/div/div[2]/a/text()','Escolha o campus certo da ELS','Escolha');

		INSERT INTO Fixes VALUES('TestCaseDetection/twitterhelpRU/support.twitter.com/groups/50-welcome-to-twitter.html','html/body/div/nav[2]/div/div/div/div/div/ul/descendant::text()','Мобильные устройства и приложения','М');

		INSERT INTO Fixes VALUES('TestCaseDetection/skyscannerML/www.skyscanner.com/index.html','html/body/div[2]/div[3]/div[1]/div/div/section/form/section[1]/fieldset[1]/div[2]/input/@placeholder','Masukkan negara, bandar atau lapangan terbang','Masukkan negara');

		INSERT INTO Fixes VALUES('TestCaseDetection/googleLoginEL/accounts.google.com/index.html','html/body/div/div[2]/div[2]/div[1]/form/div[1]/div/div[1]/div/div/input[1]/@placeholder','Εισαγάγετε τη διεύθυνση ηλεκτρονικού ταχυδρομείου σας','Εισαγάγετε ');

		INSERT INTO Fixes VALUES('TestCaseDetection/facebookBG/bg-bg.facebook.com/index.html','html/body/div/div[2]/div[1]/div/div[1]/div/div/div[2]/div[2]/div/div/div/form[1]/div[1]/div[2]/div/div/div/text()','&#x410;&#x434;&#x440;&#x435;&#x441; &#x43d;&#x430; &#x435;&#x43b;&#x435;&#x43a;&#x442;&#x440;&#x43e;&#x43d;&#x43d;&#x430; &#x43f;&#x43e;&#x449;&#x430; &#x438;&#x43b;&#x438; &#x43c;&#x43e;&#x431;&#x438;&#x43b;&#x435;&#x43d; &#x43d;&#x43e;&#x43c;&#x435;&#x440;','&#x410;&#x434;&#x440;&#x435;&#x441; &#x43d;&#x430; &#x435;&#x43b;&#x435;&#x43a;&#x442;&#x440;&#x43e;&#x43d;&#x43d;&#x430; &#x43f;&#x43e;&#x449;&#x430;');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150610134210/index.html','html/body/div[5]/div[1]/div[2]/div[1]/div/div[2]/div/ul/descendant::text()','Westin Well-Being','Westin');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150612121902/index.html','html/body/div[2]/div[4]/a[4]/span/text()','Información','Info');
		INSERT INTO Fixes VALUES('ScrapBook/data/20150612121902/index.html','html/body/div[2]/div[5]/form[3]/div/descendant::text()','id="head_srch_l_lbl">Este sitio','id="head_srch_l_lbl">Este');

		INSERT INTO Fixes VALUES('ScrapBook/data/20150618112557/index.html','html/body/header/div[3]/div[2]/ul/descendant::text()','VIDA Y NEGOCIOS','VIDA');

		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[4]/div/div/div/div/div/div[2]/form/fieldset[1]/input/@placeholder','digo postal o el aeropuerto','digo');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[2]/a/div[1]/div[1]/div[3]/text()','San Antonio Northwest - Medical Center','San Antonio Northwest');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[6]/a/div[1]/div[1]/div[3]/text()','Downtown \(centro\) - SoHo - Financial District \(distrito financiero\) zona','Downtown - SoHo - Financial District');		
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[10]/a/div[1]/div[1]/div[3]/text()','Londres Gatwick LGW zona','Londres');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[1]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;1,090','M&nbsp;1,090');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[2]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;1,849','M&nbsp;1,849');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[4]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;1,698','M&nbsp;1,698');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[5]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;2,030','M&nbsp;2,030');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[6]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;2,025','M&nbsp;2,025');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[7]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;1,118','M&nbsp;1,118');
		INSERT INTO Fixes VALUES('TestCaseDetection/hotwire/www.hotwire.com/index.html','html/body/div[4]/div[6]/div/ul/li[8]/a/div[1]/div[2]/div[2]/text()','MXN&nbsp;1,831','M&nbsp;1,831');
		""")

	    con.commit()
	    print "Done from populating Fixes DB"
	except lite.Error, e:
	    
	    if con:
		con.rollback()
		
	    print "Error %s:" % e.args[0]
	    sys.exit(1)
	    
	finally:
	    
	    if con:
		con.close() 

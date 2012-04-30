import cgi
import webapp2
from google.appengine.ext import db
from google.appengine.api import users

class GuestbookEntry(db.Model):
    author = db.StringProperty()
    email = db.StringProperty()
    date = db.DateTimeProperty(auto_now_add=True)
    message = db.StringProperty(multiline=True)

class HTMLbook(webapp2.RequestHandler):
    def get(self):
        self.response.out.write("""
        <!DOCTYPE HTML>
        <html>
        <title>Google App Engine for Python - Guestbook Demo</title>
        <body>
        <form action="sign" method="post" name="entry">
        Name: <input type="text" name="author" maxlength="100" />
        Email: <input type="text" name="email" maxlength="100" />
        Message: <textarea name="message" rows="2" cols="100"></textarea>
        <input type="submit" value="Sign Guestbook" />
        </form>
        <hr />
        """)

        GuestbookEntries = db.GqlQuery("SELECT * "
                                       "FROM GuestbookEntry "
                                       "ORDER BY date DESC")
        for Entry in GuestbookEntries:
            self.response.out.write('<a target="_blank" href="mailto:%s">' % Entry.email )
            self.response.out.write('%s</a>' % Entry.author )
            self.response.out.write(' posted the following message on %s:<br />' % Entry.date )
            self.response.out.write('%s' % cgi.escape( Entry.message ) )
            self.response.out.write('<hr />')
        
        self.response.out.write("""
        </body>
        </html>
        """)
        

class XMLbook(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/xml'
        self.response.out.write('<?xml version="1.0"?><guestbook>')
        GuestbookEntries = db.GqlQuery("SELECT * "
                                       "FROM GuestbookEntry "
                                       "ORDER BY date DESC")

        for Entry in GuestbookEntries:
            self.response.out.write("""
        <entry>
            """)
            self.response.out.write('<author>%s</author>' % Entry.author )
            self.response.out.write('<email>%s</email>' % Entry.email )
            self.response.out.write('<date>%s</date>' % Entry.date )
            self.response.out.write('<message>%s</message>' % cgi.escape( Entry.message ) )
            self.response.out.write("""            
        </entry>
        """)
        self.response.out.write("""
</guestbook>
        """)

class POSTbook(webapp2.RequestHandler):
    def post(self):
    
        self.response.out.write('Author: %s' % self.request.get('author') )
        self.response.out.write('Email: %s' % self.request.get('email') )
        self.response.out.write('Message: %s' % self.request.get('message') )
        
        Entry = GuestbookEntry()            
        Entry.author = self.request.get('author')    
        Entry.email = self.request.get('email')
        Entry.message = self.request.get('message')
        Entry.put()
        self.redirect('/')
                
app = webapp2.WSGIApplication([('/', HTMLbook),
                               ('/sign', POSTbook),
                               ('/xml', XMLbook)],
                              debug=True)

